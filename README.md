# NEXUS Private Chat — Full Java Stack Edition
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/abhishekraaj07/NEXUS-JAVA)

A complete, production-grade Java port of the NEXUS Private Chat ecosystem:
1. **nexus-backend**: Spring Boot 3.3 (Java 21) Zero-Knowledge Ephemeral WebSocket Relay Server + Embedded Cyber-Sleek Web Client.
2. **nexus-android**: Pure Native Android Studio App in Java with Web Crypto parity (ECDH P-256 + AES-256-GCM + AAD).
3. **Cross-Platform Web Client**: Hosted at `/` directly by Spring Boot for Desktop (Windows, Mac, Linux) & Mobile browsers.

---

## Key Features & Security Parity
- **End-to-End Encryption (E2EE)**:
  - ECDH Curve P-256 (`secp256r1`) for ephemeral key agreement.
  - HKDF-SHA-256 for symmetric key derivation with domain separation (`NEXUS-E2EE-v1`).
  - AES-256-GCM authenticated encryption with 96-bit fresh IV and Additional Authenticated Data (AAD).
  - Out-of-band Session Fingerprint verification (`XXXX-XXXX-XXXX`).
- **Privacy & Anti-Surveillance**:
  - `FLAG_SECURE` on Android (blocks screenshots and screen recording).
  - 1× Ephemeral View-Once photo sharing (self-destructs upon view).
  - Disappearing messages timer (5s, 10s, 30s, 1m).
  - "Burn Room" panic button to purge all ephemeral session keys instantly.
- **Zero-Knowledge Ephemeral Relay**:
  - The server only routes ciphertext envelopes; it never sees plaintext or private keys.
  - Exactly 0 bytes persistent disk or database storage.
  - Maximum 2 participants per room.
  - Automatic 15-minute inactivity room expiration.

---

## Project Structure
```
NEXUS-JAVA/
├── nexus-backend/       # Spring Boot 3.3.4 (Java 21) Relay Server
│   ├── build.gradle / pom.xml
│   └── src/main/
│       ├── java/com/nexus/backend/
│       └── resources/static/   # Embedded E2EE Web Client
└── nexus-android/       # Pure Native Android Studio App in Java
    ├── build.gradle
    ├── settings.gradle
    └── app/
        ├── build.gradle
        └── src/main/
            ├── java/com/nexus/chat/ (Crypto, UI, Network)
            └── res/layout/ (activity_main, activity_chat, item_message)
```

---

## How to Run Backend & Web Client
```bash
cd nexus-backend
./gradlew bootRun
```
* **Web Client**: Open `http://localhost:8080/` in any browser (Desktop or Mobile).
* **API Health Status**: Open `http://localhost:8080/status` or `http://localhost:8080/api/status`.

## How to Run Android App
Open `nexus-android` in Android Studio or build with Gradle:
```bash
cd nexus-android
./gradlew assembleDebug
```
* On the Android app home screen, set the Relay Server URL (e.g. `http://10.0.2.2:8080` for Android emulator, your PC's local Wi-Fi IP, or your deployed Render URL).
