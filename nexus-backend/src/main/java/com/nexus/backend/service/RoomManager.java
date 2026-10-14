package com.nexus.backend.service;

import com.nexus.backend.model.Room;
import com.nexus.backend.util.RoomCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomManager {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    @Value("${nexus.relay.inactivity-timeout-ms:900000}")
    private long inactivityTimeoutMs;

    public Room createRoom() {
        String code;
        do {
            code = RoomCodeGenerator.generateRoomCode();
        } while (rooms.containsKey(code));

        Room room = new Room(code, inactivityTimeoutMs);
        rooms.put(code, room);
        return room;
    }

    public Optional<Room> getRoom(String code) {
        if (code == null) return Optional.empty();
        String normalized = RoomCodeGenerator.normalize(code);
        Room room = rooms.get(normalized);
        if (room != null && room.isExpired()) {
            room.burn();
            rooms.remove(normalized);
            return Optional.empty();
        }
        return Optional.ofNullable(room);
    }

    public void removeRoom(String code) {
        String normalized = RoomCodeGenerator.normalize(code);
        Room room = rooms.remove(normalized);
        if (room != null) {
            room.burn();
        }
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredRooms() {
        rooms.entrySet().removeIf(entry -> {
            Room r = entry.getValue();
            if (r.isExpired()) {
                r.burn();
                return true;
            }
            return false;
        });
    }

    public int getActiveRoomCount() { return rooms.size(); }
}
