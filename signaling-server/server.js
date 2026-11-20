"use strict";

const http = require("http");
const crypto = require("crypto");
const WebSocket = require("ws");

const PORT = Number(process.env.PORT || 8080);
const PAIR_TTL_MS = 5 * 60 * 1000;
const MAX_MESSAGE_BYTES = 64 * 1024;
const rooms = new Map();
const joinAttempts = new Map();

function allowJoinAttempt(ip) {
  const now = Date.now();
  const entry = joinAttempts.get(ip) || { start: now, count: 0 };
  if (now - entry.start > 60_000) { entry.start = now; entry.count = 0; }
  entry.count += 1;
  joinAttempts.set(ip, entry);
  return entry.count <= 10;
}

const server = http.createServer((req, res) => {
  if (req.url === "/health") {
    res.writeHead(200, { "content-type": "application/json" });
    return res.end(JSON.stringify({ ok: true, service: "Reimbursement Claim signaling" }));
  }
  res.writeHead(404);
  res.end("Not found");
});

const wss = new WebSocket.Server({
  server,
  maxPayload: MAX_MESSAGE_BYTES,
  perMessageDeflate: false
});

function newCode() {
  // Six-digit, short-lived pairing code. The client must display it to its owner.
  return String(crypto.randomInt(100000, 1000000));
}

function send(ws, data) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(data));
  }
}

function closeRoom(code, reason) {
  const room = rooms.get(code);
  if (!room) return;
  clearTimeout(room.expiry);
  for (const peer of room.peers) {
    if (peer.readyState === WebSocket.OPEN) {
      send(peer, { type: "session-ended", reason });
      peer.close(1000, "Session ended");
    }
  }
  rooms.delete(code);
}

function relay(room, sender, message) {
  for (const peer of room.peers) {
    if (peer !== sender) send(peer, message);
  }
}

wss.on("connection", (ws) => {
  ws.isAlive = true;
  ws.on("pong", () => { ws.isAlive = true; });

  ws.on("message", (raw) => {
    let msg;
    try { msg = JSON.parse(raw.toString()); } catch { return send(ws, { type: "error", message: "Invalid JSON" }); }
    if (!msg || typeof msg.type !== "string") return;

    if (msg.type === "create-session") {
      if (ws.roomCode) return send(ws, { type: "error", message: "This connection already has a session" });
      let code;
      do { code = newCode(); } while (rooms.has(code));
      const room = { peers: [ws], createdAt: Date.now(), expiry: null };
      room.expiry = setTimeout(() => closeRoom(code, "Pairing code expired"), PAIR_TTL_MS);
      rooms.set(code, room);
      ws.roomCode = code;
      ws.role = "client";
      return send(ws, { type: "session-created", code, expiresInSeconds: PAIR_TTL_MS / 1000 });
    }

    if (msg.type === "join-session") {
      const forwarded = String(ws._socket?.remoteAddress || "unknown");
      const ip = forwarded.replace(/^::ffff:/, "");
      if (!allowJoinAttempt(ip)) return send(ws, { type: "error", message: "Too many pairing attempts. Try again in one minute." });
      const code = String(msg.code || "");
      const room = rooms.get(code);
      if (!/^\d{6}$/.test(code) || !room) return send(ws, { type: "error", message: "Pairing code is invalid or expired" });
      if (room.peers.length >= 2) return send(ws, { type: "error", message: "Session is already paired" });
      room.peers.push(ws);
      ws.roomCode = code;
      ws.role = "admin";
      clearTimeout(room.expiry);
      room.expiry = setTimeout(() => closeRoom(code, "Session timed out"), 60 * 60 * 1000);
      send(ws, { type: "paired", role: "admin" });
      send(room.peers[0], { type: "peer-joined" });
      return;
    }

    const code = ws.roomCode;
    const room = code && rooms.get(code);
    if (!room || room.peers.length !== 2) return send(ws, { type: "error", message: "No active paired session" });

    // Relay only WebRTC negotiation messages. The server never receives screen frames.
    if (["offer", "answer", "ice-candidate", "end-session"].includes(msg.type)) {
      if (msg.type === "end-session") return closeRoom(code, "Ended by a participant");
      if (msg.payload && JSON.stringify(msg.payload).length > MAX_MESSAGE_BYTES / 2) {
        return send(ws, { type: "error", message: "Signaling payload too large" });
      }
      relay(room, ws, { type: msg.type, payload: msg.payload });
    }
  });

  ws.on("close", () => {
    if (ws.roomCode) closeRoom(ws.roomCode, "A participant disconnected");
  });
  ws.on("error", () => {
    if (ws.roomCode) closeRoom(ws.roomCode, "Connection error");
  });
});

const heartbeat = setInterval(() => {
  for (const ws of wss.clients) {
    if (!ws.isAlive) {
      if (ws.roomCode) closeRoom(ws.roomCode, "Connection lost");
      return ws.terminate();
    }
    ws.isAlive = false;
    ws.ping();
  }
}, 30000);

server.listen(PORT, "0.0.0.0", () => {
  console.log(`Signaling server listening on port ${PORT}`);
});

function shutdown() {
  clearInterval(heartbeat);
  for (const code of rooms.keys()) closeRoom(code, "Server shutting down");
  server.close(() => process.exit(0));
}
process.on("SIGTERM", shutdown);
process.on("SIGINT", shutdown);
