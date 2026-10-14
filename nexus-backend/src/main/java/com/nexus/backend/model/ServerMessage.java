package com.nexus.backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerMessage {
    @JsonProperty("type")
    private String type;

    @JsonProperty("roomCode")
    private String roomCode;

    @JsonProperty("participantCount")
    private Integer participantCount;

    @JsonProperty("participants")
    private List<Participant> participants;

    @JsonProperty("participant")
    private Participant participant;

    @JsonProperty("participantId")
    private String participantId;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("senderId")
    private String senderId;

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("iv")
    private String iv;

    @JsonProperty("ciphertext")
    private String ciphertext;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("isTyping")
    private Boolean isTyping;

    @JsonProperty("publicKey")
    private Map<String, Object> publicKey;

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("createdAt")
    private Long createdAt;

    @JsonProperty("expiresAt")
    private Long expiresAt;

    public static ServerMessage roomState(String roomCode, int count, List<Participant> participants, long created, long expires) {
        ServerMessage m = new ServerMessage();
        m.type = "room-state";
        m.roomCode = roomCode;
        m.participantCount = count;
        m.participants = participants;
        m.createdAt = created;
        m.expiresAt = expires;
        return m;
    }

    public static ServerMessage peerJoined(Participant p, int count) {
        ServerMessage m = new ServerMessage();
        m.type = "peer-joined";
        m.participant = p;
        m.participantCount = count;
        return m;
    }

    public static ServerMessage peerLeft(String participantId, String displayName, int count) {
        ServerMessage m = new ServerMessage();
        m.type = "peer-left";
        m.participantId = participantId;
        m.displayName = displayName;
        m.participantCount = count;
        return m;
    }

    public static ServerMessage keyExchange(String senderId, Map<String, Object> publicKey) {
        ServerMessage m = new ServerMessage();
        m.type = "key-exchange";
        m.senderId = senderId;
        m.publicKey = publicKey;
        return m;
    }

    public static ServerMessage message(String senderId, String messageId, String iv, String ciphertext, long timestamp) {
        ServerMessage m = new ServerMessage();
        m.type = "message";
        m.senderId = senderId;
        m.messageId = messageId;
        m.iv = iv;
        m.ciphertext = ciphertext;
        m.timestamp = timestamp;
        return m;
    }

    public static ServerMessage typing(String senderId, boolean isTyping) {
        ServerMessage m = new ServerMessage();
        m.type = "typing";
        m.senderId = senderId;
        m.isTyping = isTyping;
        return m;
    }

    public static ServerMessage error(String code, String msg) {
        ServerMessage m = new ServerMessage();
        m.type = "error";
        m.code = code;
        m.message = msg;
        return m;
    }

    public static ServerMessage pong() {
        ServerMessage m = new ServerMessage();
        m.type = "pong";
        return m;
    }

    public String getType() { return type; }
    public String getRoomCode() { return roomCode; }
    public Integer getParticipantCount() { return participantCount; }
    public List<Participant> getParticipants() { return participants; }
    public Participant getParticipant() { return participant; }
    public String getParticipantId() { return participantId; }
    public String getDisplayName() { return displayName; }
    public String getSenderId() { return senderId; }
    public String getMessageId() { return messageId; }
    public String getIv() { return iv; }
    public String getCiphertext() { return ciphertext; }
    public Long getTimestamp() { return timestamp; }
    public Boolean getIsTyping() { return isTyping; }
    public Map<String, Object> getPublicKey() { return publicKey; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public Long getCreatedAt() { return createdAt; }
    public Long getExpiresAt() { return expiresAt; }
}
