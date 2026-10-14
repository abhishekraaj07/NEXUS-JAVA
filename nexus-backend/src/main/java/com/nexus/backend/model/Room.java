package com.nexus.backend.model;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Room {
    private final String roomCode;
    private final long createdAt;
    private final AtomicLong lastActiveAt;
    private final long inactivityTimeoutMs;
    private final Map<WebSocketSession, Participant> sessions = new ConcurrentHashMap<>();

    public Room(String roomCode, long inactivityTimeoutMs) {
        this.roomCode = roomCode;
        this.createdAt = System.currentTimeMillis();
        this.lastActiveAt = new AtomicLong(this.createdAt);
        this.inactivityTimeoutMs = inactivityTimeoutMs;
    }

    public String getRoomCode() { return roomCode; }
    public long getCreatedAt() { return createdAt; }
    public long getLastActiveAt() { return lastActiveAt.get(); }
    public long getExpiresAt() { return lastActiveAt.get() + inactivityTimeoutMs; }

    public void touch() { this.lastActiveAt.set(System.currentTimeMillis()); }
    public boolean isExpired() { return (System.currentTimeMillis() - lastActiveAt.get()) >= inactivityTimeoutMs; }
    public Map<WebSocketSession, Participant> getSessions() { return sessions; }
    public int getParticipantCount() { return sessions.size(); }
    public List<Participant> getParticipants() { return new ArrayList<>(sessions.values()); }

    public void addSession(WebSocketSession session, Participant participant) {
        sessions.put(session, participant);
        touch();
    }

    public Participant removeSession(WebSocketSession session) {
        touch();
        return sessions.remove(session);
    }

    public void burn() {
        for (WebSocketSession session : sessions.keySet()) {
            if (session.isOpen()) {
                try {
                    session.close();
                } catch (IOException ignored) {}
            }
        }
        sessions.clear();
    }
}
