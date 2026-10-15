package com.nexus.chat.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.util.Map;

public class NexusWebSocketClient extends WebSocketListener {
    public interface Listener {
        void onOpen();
        void onPeerJoined(String displayName);
        void onPeerLeft(String displayName);
        void onKeyExchange(Map<String, Object> peerJwk);
        void onMessage(String senderId, String messageId, String iv, String ciphertext, long timestamp);
        void onTyping(boolean isTyping);
        void onError(String error);
        void onClose();
    }

    private final OkHttpClient client = new OkHttpClient.Builder()
            .pingInterval(25, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final Gson gson = new Gson();
    private final String wsUrl;
    private final Listener listener;
    private WebSocket webSocket;

    public NexusWebSocketClient(String wsUrl, Listener listener) {
        this.wsUrl = wsUrl;
        this.listener = listener;
    }

    public void connect() {
        Request request = new Request.Builder().url(wsUrl).build();
        webSocket = client.newWebSocket(request, this);
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Leaving");
        }
    }

    public void send(Map<String, Object> payload) {
        if (webSocket != null) {
            webSocket.send(gson.toJson(payload));
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        listener.onOpen();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            JsonObject json = gson.fromJson(text, JsonObject.class);
            String type = json.has("type") ? json.get("type").getAsString() : "";

            switch (type) {
                case "peer-joined" -> {
                    String name = json.getAsJsonObject("participant").get("displayName").getAsString();
                    listener.onPeerJoined(name);
                }
                case "peer-left" -> {
                    String name = json.has("displayName") ? json.get("displayName").getAsString() : "Peer";
                    listener.onPeerLeft(name);
                }
                case "key-exchange" -> {
                    Map<String, Object> jwk = gson.fromJson(json.getAsJsonObject("publicKey"), Map.class);
                    listener.onKeyExchange(jwk);
                }
                case "message" -> {
                    listener.onMessage(
                            json.get("senderId").getAsString(),
                            json.get("messageId").getAsString(),
                            json.get("iv").getAsString(),
                            json.get("ciphertext").getAsString(),
                            json.get("timestamp").getAsLong()
                    );
                }
                case "typing" -> {
                    listener.onTyping(json.get("isTyping").getAsBoolean());
                }
                case "error" -> {
                    listener.onError(json.get("message").getAsString());
                }
            }
        } catch (Exception e) {
            listener.onError("Failed to parse packet: " + e.getMessage());
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        listener.onClose();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        listener.onError("WebSocket error: " + t.getMessage());
    }
}
