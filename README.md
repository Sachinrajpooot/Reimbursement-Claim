# Reimbursement Claim — Security Test Lab

**Purpose:** a clearly labelled, consent-based Android security-testing project with two separate apps. It is not an official CAPF, CRPF, government, bank, hospital, or reimbursement portal.

## Separate apps
- **Reimbursement Claim** — the test-device/client app. It runs visible checks only after a person taps a test.
- **Reimbursement Claim Admin** — a separate local demo/admin viewer. It currently imports a report only when the user selects and shares that file.

## What is implemented now
- Local device posture and permission-status checks.
- Network availability/type-change monitor (no packet capture or browsing-history collection).
- Static permission and signer review of an APK selected by the user.
- Individually requested camera, microphone and location permission tests; no sensor is activated by merely opening the app.
- Local test notification and a link to Android Notification Access settings (the app does not read other apps' notifications).
- Android screen-capture consent-prompt test only. This build does **not** capture, save, stream, or transmit screen frames.
- Exportable local text report; Admin imports only a file explicitly selected by the user.

## Remote-support development plan (not yet implemented)
The next safe milestone is an internet-based, one-session-at-a-time support flow:
1. The client phone displays a pairing code and the person on that phone starts the session.
2. Admin pairs using that short-lived code; signaling must use authenticated TLS/WSS.
3. Screen sharing starts only after Android's own MediaProjection consent prompt for that session, with a visible foreground notification and a clear Stop button.
4. WebRTC can carry the approved screen stream across different networks; a properly secured STUN/TURN service may be needed for network traversal.
5. Remote interaction, if added, will be limited to a clearly visible test area inside this app, with the client able to stop/revoke at any time. It will not provide hidden or unrestricted control of the whole phone.

**No remote connection or live screen sharing is available in the current APK.** No backend, signaling server, TURN credentials, or deployed service is configured yet. Do not install this build expecting remote access.

## Safety and privacy
- No stealth operation, hidden persistence, credential/OTP collection, keylogging, private-message extraction, or covert camera/microphone use.
- No personal content is sent to a server by the current modules.
- Use only devices you own or have explicit permission to test.
- APK permission scores are heuristics, not malware verdicts.
- This is a security-training project, not an official reimbursement service. Never enter real force IDs, passwords, or OTPs.

## Build APKs
GitHub Actions builds separate debug APKs on pushes to `main` and on manual workflow dispatch.

Open **Actions → Android APK Build**, wait for a successful run, then download:
- `Reimbursement-Claim-user-debug-apk`
- `Reimbursement-Claim-admin-debug-apk`

Minimum Android version: Android 8.0 (API 26).
