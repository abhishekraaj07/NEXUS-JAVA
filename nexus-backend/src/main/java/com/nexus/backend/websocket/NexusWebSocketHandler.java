package com.nexus.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.model.ClientMessage;
import com.nexus.backend.model.Participant;
import com.nexus.backend.model.Room;
import com.nexus.backend.model.ServerMessage;
import com.nexus.backend.service.RateLimiterService;
import com.nexus.backend.service.RoomManager;
import com.nexus.backend.util.RoomCodeGenerator;
import com.nexus.backend.util.ValidationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NexusWebSocketHandler extends TextWebSocketHandler {
    private final RoomManager roomManager;
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern PATH_PATTERN = Pattern.compile("^/api/rooms/([A-Za-z0-9_-]+)/ws$");

    public NexusWebSocketHandler(RoomManager roomManager, RateLimiterService rateLimiterService) {
        this.roomManager = roomManager;
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        String payload = textMessage.getPayload();

        if (payload.length() > ValidationUtils.MAX_FRAME_SIZE) {
            send(session, ServerMessage.error("INVALID_PAYLOAD", "Message frame exceeds 8MB limit"));
            return;
        }

        if (!ValidationUtils.isSafeJson(payload)) {
            send(session, ServerMessage.error("INVALID_PAYLOAD", "Unsafe object structure"));
            return;
        }

        ClientMessage msg = objectMapper.readValue(payload, ClientMessage.class);
        if (msg.getType() == null) {
            send(session, ServerMessage.error("INVALID_PAYLOAD", "Type must be specified"));
            return;
        }

        if ("ping".equals(msg.getType())) {
            send(session, ServerMessage.pong());
            return;
        }

        String roomCode = extractRoomCode(session);
        if (roomCode == null) {
            send(session, ServerMessage.error("ROOM_NOT_FOUND", "Room code missing in URI"));
            session.close();
            return;
        }

        Optional<Room> roomOpt = roomManager.getRoom(roomCode);
        if (roomOpt.isEmpty()) {
            send(session, ServerMessage.error("ROOM_NOT_FOUND", "Room not found or expired"));
            session.close();
            return;
        }

        Room room = roomOpt.get();

        switch (msg.getType()) {
            case "join" -> handleJoin(session, room, msg);
            case "key-exchange" -> handleKeyExchange(session, room, msg);
            case "message" -> handleMessage(session, room, msg);
            case "typing" -> handleTyping(session, room, msg);
            case "leave" -> handleLeave(session, room);
            default -> send(session, ServerMessage.error("INVALID_PAYLOAD", "Unknown type: " + msg.getType()));
        }
    }

    private void handleJoin(WebSocketSession session, Room room, ClientMessage msg) throws IOException {
        if (room.getParticipantCount() >= 2 && !room.getSessions().containsKey(session)) {
            send(session, ServerMessage.error("ROOM_FULL", "This room already has two participants."));
            session.close(new CloseStatus(4003, "Room full"));
            return;
        }

        String participantId = msg.getParticipantId() != null ? msg.getParticipantId() : session.getId();
        String safeName = ValidationUtils.sanitizeDisplayName(msg.getDisplayName());
        Participant participant = new Participant(participantId, safeName, System.currentTimeMillis());

        room.addSession(session, participant);

        send(session, ServerMessage.roomState(
                room.getRoomCode(),
                room.getParticipantCount(),
                room.getParticipants(),
                room.getCreatedAt(),
                room.getExpiresAt()
        ));

        broadcastExcept(session, room, ServerMessage.peerJoined(participant, room.getParticipantCount()));
    }

    private void handleKeyExchange(WebSocketSession session, Room room, ClientMessage msg) throws IOException {
        Participant sender = room.getSessions().get(session);
        if (sender == null) {
            send(session, ServerMessage.error("UNAUTHORIZED", "Must join before exchanging keys"));
            return;
        }
        room.touch();
        broadcastExcept(session, room, ServerMessage.keyExchange(sender.getId(), msg.getPublicKey()));
    }

    private void handleMessage(WebSocketSession session, Room room, ClientMessage msg) throws IOException {
        Participant sender = room.getSessions().get(session);
        if (sender == null) {
            send(session, ServerMessage.error("UNAUTHORIZED", "Must join before sending messages"));
            return;
        }

        if (!rateLimiterService.isAllowed("msg_" + sender.getId(), 30, 10000)) {
            send(session, ServerMessage.error("RATE_LIMITED", "Message rate limit exceeded"));
            return;
        }

        room.touch();
        broadcastExcept(session, room, ServerMessage.message(
                sender.getId(),
                msg.getMessageId(),
                msg.getIv(),
                msg.getCiphertext(),
                msg.getTimestamp() != null ? msg.getTimestamp() : System.currentTimeMillis()
        ));
    }

    private void handleTyping(WebSocketSession session, Room room, ClientMessage msg) throws IOException {
        Participant sender = room.getSessions().get(session);
        if (sender != null) {
            boolean isTyping = Boolean.TRUE.equals(msg.getIsTyping());
            broadcastExcept(session, room, ServerMessage.typing(sender.getId(), isTyping));
        }
    }

    private void handleLeave(WebSocketSession session, Room room) throws IOException {
        Participant p = room.removeSession(session);
        if (p != null) {
            broadcast(room, ServerMessage.peerLeft(p.getId(), p.getDisplayName(), room.getParticipantCount()));
        }
        session.close();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomCode = extractRoomCode(session);
        if (roomCode != null) {
            roomManager.getRoom(roomCode).ifPresent(room -> {
                Participant p = room.removeSession(session);
                if (p != null) {
                    try {
                        broadcast(room, ServerMessage.peerLeft(p.getId(), p.getDisplayName(), room.getParticipantCount()));
                    } catch (IOException ignored) {}
                }
            });
        }
    }

    private String extractRoomCode(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        Matcher m = PATH_PATTERN.matcher(uri.getPath());
        if (m.matches()) {
            return RoomCodeGenerator.normalize(m.group(1));
        }
        return null;
    }

    private void send(WebSocketSession session, ServerMessage msg) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        }
    }

    private void broadcast(Room room, ServerMessage msg) throws IOException {
        String json = objectMapper.writeValueAsString(msg);
        for (WebSocketSession s : room.getSessions().keySet()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    private void broadcastExcept(WebSocketSession excluded, Room room, ServerMessage msg) throws IOException {
        String json = objectMapper.writeValueAsString(msg);
        for (WebSocketSession s : room.getSessions().keySet()) {
            if (s.isOpen() && !s.getId().equals(excluded.getId())) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }
}
