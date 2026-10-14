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
    // Default relay endpoint
    public static final String DEFAULT_SERVER = "https://nexus-private-chat.abhishekraaj07.workers.dev";

    private EditText etRoomCode;
    private NexusApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiClient = new NexusApiClient(DEFAULT_SERVER);

        Button btnCreate = findViewById(R.id.btn_create_room);
        Button btnJoin = findViewById(R.id.btn_join_room);
        etRoomCode = findViewById(R.id.et_room_code);

        btnCreate.setOnClickListener(v -> createRoom());
        btnJoin.setOnClickListener(v -> joinRoom());
    }

    private void createRoom() {
        apiClient.createRoom(new NexusApiClient.RoomCallback() {
            @Override
            public void onSuccess(String roomCode) {
                runOnUiThread(() -> openChat(roomCode, "Creator"));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void joinRoom() {
        String code = etRoomCode.getText().toString().trim().toUpperCase();
        if (code.length() < 6) {
            Toast.makeText(this, "Enter valid 6-char room code", Toast.LENGTH_SHORT).show();
            return;
        }
        openChat(code, "Guest");
    }

    private void openChat(String roomCode, String displayName) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ROOM_CODE", roomCode);
        intent.putExtra("DISPLAY_NAME", displayName);
        intent.putExtra("SERVER_URL", DEFAULT_SERVER);
        startActivity(intent);
    }
}
