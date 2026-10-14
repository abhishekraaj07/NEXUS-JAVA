# NEXUS Private Chat — Full Java Stack Edition
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/abhishekraaj07/NEXUS-JAVA)


A complete, production-grade Java port of the NEXUS Private Chat ecosystem:
1. **nexus-backend**: Spring Boot 3.3 (Java 21) Zero-Knowledge Ephemeral WebSocket Relay Server.
2. **nexus-android**: Pure Native Android Studio App in Java with Web Crypto parity (ECDH P-256 + AES-256-GCM + AAD).

---

## Key Features & Security Parity
- **End-to-End Encryption (E2EE)**:
  - ECDH Curve P-256 (`secp256r1`) for ephemeral key agreement.
  - HKDF-SHA-256 for symmetric key derivation with domain separation (`NEXUS-E2EE-v1`).
  - AES-256-GCM authenticated encryption with 96-bit fresh IV and Additional Authenticated Data (AAD).
  - Out-of-band Session Fingerprint verification (`XXXX-XXXX-XXXX`).
- **Zero-Knowledge Ephemeral Relay**:
  - The server only routes ciphertext envelopes; it never sees plaintext or private keys.
  - Exactly 0 bytes persistent disk or database storage.
  - Maximum 2 participants per room.
  - Automatic 15-minute inactivity room expiration.

---

## Project Structure
```
C:\NEXUS-JAVA\
├── nexus-backend/       # Spring Boot 3.3.4 (Java 21) Relay Server
│   ├── pom.xml
│   └── src/main/java/com/nexus/backend/
└── nexus-android/       # Pure Native Android Studio App in Java
    ├── build.gradle
    ├── settings.gradle
    └── app/
        ├── build.gradle
        └── src/main/java/com/nexus/chat/
```

---

## How to Run Backend
```bash
cd nexus-backend
mvn spring-boot:run
```
Server starts on `http://localhost:8080`.

## How to Run Android App
Open `nexus-android` in Android Studio or build with Gradle:
```bash
cd nexus-android
./gradlew assembleDebug
```
