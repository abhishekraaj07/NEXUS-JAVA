package com.nexus.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Participant {
    @JsonProperty("id")
    private String id;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("joinedAt")
    private long joinedAt;

    public Participant() {}

    public Participant(String id, String displayName, long joinedAt) {
        this.id = id;
        this.displayName = displayName;
        this.joinedAt = joinedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }
}
