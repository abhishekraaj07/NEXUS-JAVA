package com.nexus.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientMessage {
    @JsonProperty("type")
    private String type;

    @JsonProperty("roomCode")
    private String roomCode;

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

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getIv() { return iv; }
    public void setIv(String iv) { this.iv = iv; }
    public String getCiphertext() { return ciphertext; }
    public void setCiphertext(String ciphertext) { this.ciphertext = ciphertext; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public Boolean getIsTyping() { return isTyping; }
    public void setIsTyping(Boolean typing) { isTyping = typing; }
    public Map<String, Object> getPublicKey() { return publicKey; }
    public void setPublicKey(Map<String, Object> publicKey) { this.publicKey = publicKey; }
}
