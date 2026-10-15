package com.nexus.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.nexus.chat.R;
import com.nexus.chat.network.NexusApiClient;

public class MainActivity extends AppCompatActivity {
    public static final String DEFAULT_SERVER = "https://nexus-backend-f44m.onrender.com";

    private EditText etRoomCode;
    private EditText etServerUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCreate = findViewById(R.id.btn_create_room);
        Button btnJoin = findViewById(R.id.btn_join_room);
        etRoomCode = findViewById(R.id.et_room_code);
        etServerUrl = findViewById(R.id.et_server_url);

        btnCreate.setOnClickListener(v -> createRoom());
        btnJoin.setOnClickListener(v -> joinRoom());
    }

    private String getSelectedServer() {
        String url = etServerUrl.getText().toString().trim();
        if (url.isEmpty()) return DEFAULT_SERVER;
        return url;
    }

    private void createRoom() {
        String server = getSelectedServer();
        NexusApiClient apiClient = new NexusApiClient(server);

        apiClient.createRoom(new NexusApiClient.RoomCallback() {
            @Override
            public void onSuccess(String roomCode) {
                runOnUiThread(() -> openChat(roomCode, "Creator", server));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Server error: " + message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void joinRoom() {
        String code = etRoomCode.getText().toString().trim().toUpperCase();
        if (code.length() < 5) {
            Toast.makeText(this, "Enter valid room code", Toast.LENGTH_SHORT).show();
            return;
        }
        openChat(code, "Guest", getSelectedServer());
    }

    private void openChat(String roomCode, String displayName, String serverUrl) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ROOM_CODE", roomCode);
        intent.putExtra("DISPLAY_NAME", displayName);
        intent.putExtra("SERVER_URL", serverUrl);
        startActivity(intent);
    }
}
