# Reimbursement Claim signaling service

This service handles short-lived pairing and WebRTC negotiation only. It does not capture screens, store recordings, relay video, or issue device-control commands.

## Deploy
Deploy this folder as a Node.js web service on a hosting provider that supports WebSockets. Set the start command to `npm start`. Use the provider's HTTPS/WSS address in the Android apps. Do not use plain ws:// on the public internet.

## Protocol
1. Client phone opens WSS and sends `{"type":"create-session"}`.
2. Server returns a six-digit code valid for five minutes.
3. Admin app enters the code and sends `{"type":"join-session","code":"123456"}`.
4. After pairing, the peers may exchange WebRTC `offer`, `answer`, and `ice-candidate` messages.
5. Either side can send `{"type":"end-session"}`; disconnect also closes the room.

The code is a pairing convenience, not a substitute for strong authentication. Production deployment should add TLS at the host, connection/IP rate limits, abuse monitoring, and a second confirmation on the client device before any screen-sharing permission is requested.

## Important
This is only the signaling foundation. The Android WebRTC client, per-session Android MediaProjection consent, visible foreground notification, and user-controlled Stop action are not included yet. No remote screen or touch control works from this server alone.
