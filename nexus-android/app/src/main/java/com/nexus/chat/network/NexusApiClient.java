package com.nexus.chat.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;

public class NexusApiClient {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String baseUrl;

    public NexusApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public interface RoomCallback {
        void onSuccess(String roomCode);
        void onError(String message);
    }

    public void createRoom(RoomCallback callback) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{}", mediaType);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/rooms")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Connection failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Error " + response.code());
                    return;
                }
                String body = response.body().string();
                JsonObject json = gson.fromJson(body, JsonObject.class);
                if (json.has("roomCode")) {
                    callback.onSuccess(json.get("roomCode").getAsString());
                } else {
                    callback.onError("Invalid server response");
                }
            }
        });
    }
}
