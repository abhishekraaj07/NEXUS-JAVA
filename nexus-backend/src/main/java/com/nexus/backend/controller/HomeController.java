package com.nexus.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;

@RestController
public class HomeController {

    private final Instant startTime = Instant.now();

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>NEXUS - Secure Ephemeral Relay</title>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;700&family=Plus+Jakarta+Sans:wght@400;600;700;800&display=swap" rel="stylesheet">
                <style>
                    :root {
                        --bg: #090b10;
                        --card-bg: rgba(18, 22, 33, 0.75);
                        --card-border: rgba(99, 102, 241, 0.18);
                        --primary: #6366f1;
                        --primary-glow: rgba(99, 102, 241, 0.35);
                        --accent: #22d3ee;
                        --success: #10b981;
                        --text-main: #f8fafc;
                        --text-muted: #94a3b8;
                    }
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Plus Jakarta Sans', -apple-system, sans-serif;
                        background: var(--bg);
                        color: var(--text-main);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 24px;
                        background-image: 
                            radial-gradient(ellipse 80% 50% at 50% -20%, rgba(99, 102, 241, 0.25), transparent),
                            radial-gradient(ellipse 60% 40% at 50% 120%, rgba(34, 211, 238, 0.15), transparent);
                    }
                    .container {
                        max-width: 680px;
                        width: 100%;
                        background: var(--card-bg);
                        border: 1px solid var(--card-border);
                        border-radius: 24px;
                        padding: 40px;
                        backdrop-filter: blur(20px);
                        box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.6), 0 0 40px var(--primary-glow);
                    }
                    .header {
                        display: flex;
                        align-items: center;
                        gap: 16px;
                        margin-bottom: 24px;
                    }
                    .logo-icon {
                        width: 52px;
                        height: 52px;
                        background: linear-gradient(135deg, #6366f1, #22d3ee);
                        border-radius: 14px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        box-shadow: 0 8px 20px rgba(99, 102, 241, 0.4);
                    }
                    .logo-icon svg { width: 28px; height: 28px; fill: white; }
                    .brand-name {
                        font-size: 26px;
                        font-weight: 800;
                        letter-spacing: -0.03em;
                        background: linear-gradient(135deg, #ffffff, #cbd5e1);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                    }
                    .brand-tag {
                        font-size: 13px;
                        color: var(--accent);
                        font-family: 'JetBrains Mono', monospace;
                        letter-spacing: 0.05em;
                    }
                    .status-badge {
                        margin-left: auto;
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        padding: 6px 14px;
                        background: rgba(16, 185, 129, 0.12);
                        border: 1px solid rgba(16, 185, 129, 0.3);
                        border-radius: 9999px;
                        font-size: 12px;
                        font-weight: 700;
                        color: var(--success);
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                    }
                    .pulse {
                        width: 8px;
                        height: 8px;
                        border-radius: 50%;
                        background: var(--success);
                        box-shadow: 0 0 10px var(--success);
                        animation: pulse-dot 1.8s infinite;
                    }
                    @keyframes pulse-dot {
                        0%, 100% { opacity: 1; transform: scale(1); }
                        50% { opacity: 0.4; transform: scale(0.85); }
                    }
                    .description {
                        font-size: 15px;
                        line-height: 1.6;
                        color: var(--text-muted);
                        margin-bottom: 28px;
                    }
                    .grid {
                        display: grid;
                        grid-template-columns: repeat(2, 1fr);
                        gap: 12px;
                        margin-bottom: 28px;
                    }
                    @media (max-width: 580px) { .grid { grid-template-columns: 1fr; } }
                    .stat-card {
                        background: rgba(255, 255, 255, 0.03);
                        border: 1px solid rgba(255, 255, 255, 0.06);
                        border-radius: 14px;
                        padding: 16px;
                    }
                    .stat-title {
                        font-size: 11px;
                        color: var(--text-muted);
                        text-transform: uppercase;
                        letter-spacing: 0.06em;
                        margin-bottom: 6px;
                    }
                    .stat-val {
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 13px;
                        color: #f1f5f9;
                        font-weight: 600;
                    }
                    .endpoints {
                        background: rgba(15, 23, 42, 0.6);
                        border: 1px solid rgba(255, 255, 255, 0.06);
                        border-radius: 14px;
                        padding: 18px;
                        margin-bottom: 28px;
                    }
                    .endpoints-title {
                        font-size: 12px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                        color: var(--accent);
                        margin-bottom: 12px;
                    }
                    .endpoint-row {
                        display: flex;
                        align-items: center;
                        gap: 10px;
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 12px;
                        padding: 6px 0;
                        border-bottom: 1px solid rgba(255, 255, 255, 0.04);
                    }
                    .endpoint-row:last-child { border-bottom: none; }
                    .method {
                        font-weight: 700;
                        padding: 2px 6px;
                        border-radius: 4px;
                        font-size: 10px;
                    }
                    .method-post { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
                    .method-get  { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
                    .method-ws   { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
                    .path { color: #e2e8f0; }
                    .path-desc { margin-left: auto; color: var(--text-muted); font-size: 11px; }
                    .footer {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        font-size: 12px;
                        color: var(--text-muted);
                        border-top: 1px solid rgba(255, 255, 255, 0.06);
                        padding-top: 20px;
                    }
                    .footer a {
                        color: var(--accent);
                        text-decoration: none;
                        font-weight: 600;
                        transition: color 0.15s ease;
                    }
                    .footer a:hover { color: #67e8f9; text-decoration: underline; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo-icon">
                            <svg viewBox="0 0 24 24"><path d="M12 2L3 7v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-9-5zm0 2.18l7 3.89v5.93c0 4.54-3.14 8.79-7 9.88-3.86-1.09-7-5.34-7-9.88V8.07l7-3.89zM12 7a3 3 0 00-3 3c0 1.31.84 2.42 2 2.83V16a1 1 0 102 0v-3.17c1.16-.41 2-1.52 2-2.83a3 3 0 00-3-3z"/></svg>
                        </div>
                        <div>
                            <div class="brand-name">NEXUS</div>
                            <div class="brand-tag">JAVA 21 &bull; SECURE RELAY</div>
                        </div>
                        <div class="status-badge">
                            <span class="pulse"></span>
                            Operational
                        </div>
                    </div>

                    <div class="description">
                        High-performance zero-knowledge ephemeral WebSocket relay. No chats or user keys are ever written to disk or database. All cryptographic handshakes (ECDH P-256 + HKDF + AES-256-GCM) take place strictly client-side.
                    </div>

                    <div class="grid">
                        <div class="stat-card">
                            <div class="stat-title">Runtime Environment</div>
                            <div class="stat-val">Spring Boot 3.3.4 (Java 21)</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-title">Cryptographic Protocol</div>
                            <div class="stat-val">E2EE ECDH + AES-GCM</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-title">Storage Persistence</div>
                            <div class="stat-val">0 Bytes (In-Memory Only)</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-title">Room Expiration TTL</div>
                            <div class="stat-val">15 Minutes Inactivity</div>
                        </div>
                    </div>

                    <div class="endpoints">
                        <div class="endpoints-title">Active API Endpoints</div>
                        <div class="endpoint-row">
                            <span class="method method-post">POST</span>
                            <span class="path">/api/rooms</span>
                            <span class="path-desc">Create secure room</span>
                        </div>
                        <div class="endpoint-row">
                            <span class="method method-get">GET</span>
                            <span class="path">/api/rooms/{code}/info</span>
                            <span class="path-desc">Validate room code</span>
                        </div>
                        <div class="endpoint-row">
                            <span class="method method-ws">WS</span>
                            <span class="path">/api/rooms/{code}/ws</span>
                            <span class="path-desc">Encrypted duplex relay</span>
                        </div>
                        <div class="endpoint-row">
                            <span class="method method-get">GET</span>
                            <span class="path">/api/status</span>
                            <span class="path-desc">Health & JSON metrics</span>
                        </div>
                    </div>

                    <div class="footer">
                        <span>Client: Android Native / Web</span>
                        <a href="https://github.com/abhishekraaj07/NEXUS-JAVA" target="_blank" rel="noopener">GitHub Repository &rarr;</a>
                    </div>
                </div>
            </body>
            </html>
            """;
        return ResponseEntity.ok(html);
    }

    @GetMapping(value = "/api/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> status() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "NEXUS-JAVA-RELAY",
                "version", "1.0.0",
                "javaVersion", System.getProperty("java.version"),
                "startedAt", startTime.toString(),
                "uptimeSeconds", uptimeMs / 1000
        ));
    }
}
