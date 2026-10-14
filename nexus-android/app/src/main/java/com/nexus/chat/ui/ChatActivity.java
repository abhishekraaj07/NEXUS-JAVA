package com.nexus.chat.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.nexus.chat.R;
import com.nexus.chat.crypto.NexusCrypto;
import com.nexus.chat.model.Message;
import com.nexus.chat.network.NexusWebSocketClient;

import java.util.*;

public class ChatActivity extends AppCompatActivity implements NexusWebSocketClient.Listener {
    private String roomCode;
    private String displayName;
    private String participantId;
    private NexusCrypto crypto;
    private NexusWebSocketClient wsClient;
    private final List<Message> messages = new ArrayList<>();
    private final Gson gson = new Gson();

    private TextView tvRoomCode;
    private Button btnFingerprint;
    private EditText etMessage;
    private RecyclerView rvMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        displayName = getIntent().getStringExtra("DISPLAY_NAME");
        participantId = "user_" + UUID.randomUUID().toString().substring(0, 8);

        tvRoomCode = findViewById(R.id.tv_room_code);
        btnFingerprint = findViewById(R.id.btn_fingerprint);
        Button btnLeave = findViewById(R.id.btn_leave);
        etMessage = findViewById(R.id.et_message);
        Button btnSend = findViewById(R.id.btn_send);
        rvMessages = findViewById(R.id.rv_messages);

        tvRoomCode.setText("ROOM: " + roomCode);

        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        try {
            crypto = new NexusCrypto();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to init E2EE engine", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String wsUrl = "wss://nexus-private-chat.abhishekraaj07.workers.dev/api/rooms/" + roomCode + "/ws";
        wsClient = new NexusWebSocketClient(wsUrl, this);
        wsClient.connect();

        btnSend.setOnClickListener(v -> sendMessage());
        btnLeave.setOnClickListener(v -> finish());
        btnFingerprint.setOnClickListener(v -> showFingerprintDialog());
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        if (!crypto.isEncrypted()) {
            Toast.makeText(this, "Waiting for peer key exchange...", Toast.LENGTH_SHORT).show();
            return;
        }

        String msgId = UUID.randomUUID().toString();
        try {
            NexusCrypto.EncryptedPayload enc = crypto.encrypt(text, roomCode, participantId, msgId);

            Map<String, Object> packet = new HashMap<>();
            packet.put("type", "message");
            packet.put("roomCode", roomCode);
            packet.put("senderId", participantId);
            packet.put("messageId", msgId);
            packet.put("iv", enc.iv);
            packet.put("ciphertext", enc.ciphertext);
            packet.put("timestamp", System.currentTimeMillis());

            wsClient.send(packet);

            messages.add(new Message(msgId, participantId, displayName, text, System.currentTimeMillis(), true, "sent"));
            etMessage.setText("");
            Toast.makeText(this, "Sent: " + text, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Encryption failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showFingerprintDialog() {
        String fp = crypto.getSessionFingerprint() != null ? crypto.getSessionFingerprint() : "NOT CONNECTED";
        new AlertDialog.Builder(this)
                .setTitle("Security Fingerprint")
                .setMessage("Compare this fingerprint with your peer through a trusted channel:

" + fp +
                        "

Both participants must see the exact same fingerprint.")
                .setPositiveButton("VERIFY", null)
                .show();
    }

    @Override
    public void onOpen() {
        // Send join frame
        Map<String, Object> join = new HashMap<>();
        join.put("type", "join");
        join.put("roomCode", roomCode);
        join.put("participantId", participantId);
        join.put("displayName", displayName);
        wsClient.send(join);

        // Send key exchange frame
        Map<String, Object> kx = new HashMap<>();
        kx.put("type", "key-exchange");
        kx.put("roomCode", roomCode);
        kx.put("senderId", participantId);
        kx.put("publicKey", crypto.exportPublicKeyJwk());
        wsClient.send(kx);
    }

    @Override
    public void onPeerJoined(String name) {
        runOnUiThread(() -> Toast.makeText(this, name + " joined the room", Toast.LENGTH_SHORT).show());
        // Re-send public key so new peer can derive secret
        Map<String, Object> kx = new HashMap<>();
        kx.put("type", "key-exchange");
        kx.put("roomCode", roomCode);
        kx.put("senderId", participantId);
        kx.put("publicKey", crypto.exportPublicKeyJwk());
        wsClient.send(kx);
    }

    @Override
    public void onPeerLeft(String name) {
        runOnUiThread(() -> Toast.makeText(this, name + " left the room", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onKeyExchange(Map<String, Object> peerJwk) {
        try {
            crypto.establishSharedSecret(peerJwk);
            runOnUiThread(() -> {
                btnFingerprint.setText("SECURE (" + crypto.getSessionFingerprint() + ")");
                Toast.makeText(this, "E2EE Established!", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Key exchange failed", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onMessage(String senderId, String messageId, String iv, String ciphertext, long timestamp) {
        if (senderId.equals(participantId)) return;
        try {
            String plain = crypto.decrypt(ciphertext, iv, roomCode, senderId, messageId);
            runOnUiThread(() -> {
                messages.add(new Message(messageId, senderId, "Peer", plain, timestamp, false, "delivered"));
                Toast.makeText(this, "Peer: " + plain, Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Decryption error", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onTyping(boolean isTyping) {}
    @Override
    public void onError(String error) {
        runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onClose() {
        runOnUiThread(() -> Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wsClient != null) wsClient.disconnect();
    }
}
