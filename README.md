# 🛡️ NEXUS Private Chat — Full Java Stack Edition

<p align="center">
  <strong>Zero-Knowledge, Zero-Trace Ephemeral Chat Ecosystem in Pure Java & Web Standards</strong>
</p>

<p align="center">
  <a href="https://github.com/abhishekraaj07/NEXUS-JAVA/actions/workflows/ci.yml"><img src="https://github.com/abhishekraaj07/NEXUS-JAVA/actions/workflows/ci.yml/badge.svg" alt="Java CI" /></a>
  <a href="https://github.com/abhishekraaj07/NEXUS-JAVA/actions/workflows/build-apk.yml"><img src="https://github.com/abhishekraaj07/NEXUS-JAVA/actions/workflows/build-apk.yml/badge.svg" alt="Build APK" /></a>
  <img src="https://img.shields.io/badge/Java-21%20LTS-ED8B00?logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Android-Native%20Java-3DDC84?logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Security-E2EE%20(ECDH%20%2B%20AES--GCM)-blueviolet" alt="E2EE" />
  <img src="https://img.shields.io/badge/Storage-0%20Bytes%20Disk-crimson" alt="Zero Storage" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License" />
</p>

---

## ⚡ Overview

**NEXUS-JAVA** is an end-to-end cryptographic private messaging suite built on Java 21 and modern Web standards. It provides full protocol and security parity with zero persistent data retention:

1. **`nexus-backend`**: High-performance Spring Boot 3.3.4 ephemeral WebSocket relay server with an embedded, zero-dependency Cyber-Sleek Web Client.
2. **`nexus-android`**: Pure Native Android Studio App in Java featuring Web Crypto parity (`ECDH NIST P-256` + `AES-256-GCM`), anti-screenshot enforcement (`FLAG_SECURE`), and native chat bubble rendering.
3. **Cross-Platform Web Client**: Hosted at `/` directly by the Spring Boot server for any desktop (Windows, macOS, Linux) or mobile browser.

---

## 🔒 Cryptographic Architecture & Security Guarantee

```
                    ┌────────────────────────────────────────────────────────┐
                    │               NEXUS ZERO-KNOWLEDGE E2EE                │
                    └────────────────────────────────────────────────────────┘

     Client A (Web / Android)                                  Client B (Web / Android)
  ┌─────────────────────────────┐                           ┌─────────────────────────────┐
  │ 1. Generate NIST P-256      │                           │ 1. Generate NIST P-256      │
  │    Ephemeral Keypair        │                           │    Ephemeral Keypair        │
  │                             │                           │                             │
  │ 2. Public Key Export (JWK)  │ ── [ Relay Server ] ────> │ 2. Compute ECDH Shared      │
  │                             │ <── [ Forward Only ] ──── │    Secret & Derive AES-256  │
  │ 3. HKDF-SHA256 Derivation   │                           │    via HKDF ("NEXUS-E2EE-v1")
  │    (AES-256-GCM + 96b IV)   │                           │                             │
  │                             │                           │ 3. Compute Fingerprint:     │
  │ 4. Compute Fingerprint:     │                           │    SHA256(KeyA || KeyB)     │
  │    XXXX-XXXX-XXXX           │ <═ Out-of-band Match ═══> │    XXXX-XXXX-XXXX           │
  │                             │                           │                             │
  │ 5. Encrypt (Payload + AAD)  │ ──── Ciphertext Envelope ─> 4. Decrypt & Verify AAD     │
  └─────────────────────────────┘                           └─────────────────────────────┘
                                           │
                                ┌──────────────────────┐
                                │ Spring Boot Relay    │
                                │ • Exact 0 bytes disk │
                                │ • Never sees keys    │
                                │ • 15m Auto-Expire    │
                                └──────────────────────┘
```

### Key Security Properties:
* **End-to-End Encryption (E2EE)**: Ephemeral ECDH Curve P-256 (`secp256r1`) agreement with HKDF-SHA-256 expansion (`NEXUS-E2EE-v1` domain context).
* **Authenticated Cipher**: AES-256-GCM with fresh 96-bit cryptographically random IVs and Additional Authenticated Data (`AAD = roomCode:senderId:msgId`).
* **Out-of-Band Fingerprint**: Deterministic Session Fingerprint (`XXXX-XXXX-XXXX`) derived from canonically sorted public keys to prevent Man-in-the-Middle (MITM) attacks.
* **Ephemeral 1× Media**: View-once self-destructing image transmission.
* **Disappearing Messages**: Automated client-side TTL purge (5s, 10s, 30s, 1m).
* **Anti-Screenshot Shield**: `FLAG_SECURE` strictly enforced on Android to block screen captures and task switch previews.
* **Burn Room**: Instant panic button that purges all session keys, closes the WebSocket, and destroys the room state.
* **Zero Storage**: Exactly 0 bytes stored in database or disk. All messages live purely in transient memory during transit.

---

## 📁 Repository Structure

```
NEXUS-JAVA/
├── .github/workflows/
│   ├── build-apk.yml            # Automated Android APK Build Workflow
│   └── ci.yml                   # Spring Boot Java 21 CI Build & Test
├── nexus-backend/               # Spring Boot 3.3.4 (Java 21) Relay
│   ├── build.gradle / pom.xml
│   ├── Dockerfile               # Containerized deployment config
│   └── src/main/
│       ├── java/com/nexus/backend/
│       │   ├── config/          # WebSocket & HTTP CORS setup
│       │   ├── controller/      # RoomController (/api/rooms), HomeController
│       │   ├── model/           # Room, Participant, ClientMessage, ServerMessage
│       │   ├── service/         # RoomManager (15m expiry), RateLimiterService
│       │   ├── util/            # RoomCodeGenerator, ValidationUtils
│       │   └── websocket/       # NexusWebSocketHandler (/api/rooms/{code}/ws)
│       └── resources/
│           ├── application.yml
│           └── static/
│               └── index.html   # Full Embedded E2EE Cyber-Sleek Web Client
├── nexus-android/               # Pure Native Android Studio App (Java)
│   ├── build.gradle
│   ├── settings.gradle
│   └── app/
│       ├── build.gradle
│       └── src/main/
│           ├── AndroidManifest.xml
│           ├── java/com/nexus/chat/
│           │   ├── crypto/      # NexusCrypto (ECDH P-256 + AES-GCM Java engine)
│           │   ├── model/       # Message data structures
│           │   ├── network/     # NexusApiClient, NexusWebSocketClient
│           │   └── ui/          # MainActivity, ChatActivity, MessageAdapter
│           └── res/layout/      # activity_main, activity_chat, item_message
└── docker-compose.yml           # Local multi-container launch
```

---

## 🚀 Quickstart Guide

### 1. Run Spring Boot Backend & Web Client

**Prerequisites:** Java 21 LTS installed.

Using Gradle Wrapper:
```bash
cd nexus-backend
./gradlew bootRun
```

Using Maven:
```bash
cd nexus-backend
mvn spring-boot:run
```

* **Web Client**: Open your browser at **`http://localhost:8080/`** to immediately start an encrypted chat session.
* **Server Health Status**: Inspect **`http://localhost:8080/status`** or **`http://localhost:8080/api/status`**.

---

### 2. Run Native Android App

Open the `nexus-android` folder in **Android Studio** (Koala / Ladybug or newer) or compile directly via Gradle:

```bash
cd nexus-android
./gradlew assembleDebug
```
The compiled debug APK will be generated at:
`nexus-android/app/build/outputs/apk/debug/app-debug.apk`

#### Connecting Android App to Backend:
1. **Android Emulator**: In the `RELAY SERVER URL` field on the app's start screen, use `http://10.0.2.2:8080`.
2. **Physical Phone on Wi-Fi**: Enter your host machine's local IP (e.g. `http://192.168.1.50:8080`).
3. **Live Server**: Enter your deployed domain (e.g. `https://your-domain.com`).

---

## 🌐 API & Protocol Specification

### REST Endpoints
| Method | Path | Description | Response |
|---|---|---|---|
| `GET` | `/` | Serves the full E2EE Web Client | HTML / Web Application |
| `GET` | `/status` | Relay health status dashboard | HTML Status Page |
| `GET` | `/api/status` | JSON service health & uptime metrics | `{"status":"UP", "uptimeSeconds":...}` |
| `POST` | `/api/rooms` | Generates a new 15-minute ephemeral room | `{"roomCode":"NEX-XXXX", "createdAt":...}` |
| `GET` | `/api/rooms/{code}/info` | Verifies room existence and occupancy | `{"roomCode":"...", "participantCount":1}` |

### WebSocket Relay Protocol
Connect at: `ws://<host>/api/rooms/{roomCode}/ws` (or `wss://` over SSL)

* **Join Frame**: `{"type":"join", "roomCode":"...", "participantId":"...", "displayName":"..."}`
* **Key Exchange**: `{"type":"key-exchange", "roomCode":"...", "senderId":"...", "publicKey":{...JWK...}}`
* **Ciphertext Message**: `{"type":"message", "roomCode":"...", "senderId":"...", "messageId":"...", "iv":"...", "ciphertext":"...", "timestamp":...}`
* **Typing Indicator**: `{"type":"typing", "roomCode":"...", "senderId":"...", "isTyping":true}`
* **Heartbeat**: Client sends `{"type":"ping"}`, server returns `{"type":"pong"}`.

---

## ⚖️ License
Released under the open-source [MIT License](LICENSE).
