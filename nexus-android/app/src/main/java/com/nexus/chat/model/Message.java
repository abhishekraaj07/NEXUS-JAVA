package com.nexus.chat.model;

public class Message {
    public final String id;
    public final String senderId;
    public final String senderName;
    public final String text;
    public final long timestamp;
    public final boolean isOwn;
    public String status; // sending, sent, delivered, read, failed

    public Message(String id, String senderId, String senderName, String text, long timestamp, boolean isOwn, String status) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.isOwn = isOwn;
        this.status = status;
    }
}
