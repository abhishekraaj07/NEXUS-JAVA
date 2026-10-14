package com.nexus.backend.controller;

import com.nexus.backend.model.Room;
import com.nexus.backend.service.RateLimiterService;
import com.nexus.backend.service.RoomManager;
import com.nexus.backend.util.RoomCodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomManager roomManager;
    private final RateLimiterService rateLimiterService;

    public RoomController(RoomManager roomManager, RateLimiterService rateLimiterService) {
        this.roomManager = roomManager;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping
    public ResponseEntity<?> createRoom(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        if (!rateLimiterService.isAllowed("create_" + clientIp, 10, 60000)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many rooms created. Please wait."));
        }

        Room room = roomManager.createRoom();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "roomCode", room.getRoomCode(),
                "createdAt", room.getCreatedAt(),
                "expiresInMinutes", 15
        ));
    }

    @GetMapping("/{code}/info")
    public ResponseEntity<?> getRoomInfo(@PathVariable("code") String code, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        if (!rateLimiterService.isAllowed("info_" + clientIp, 20, 60000)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many attempts. Please wait."));
        }

        if (!RoomCodeGenerator.isValid(code)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid room code format"));
        }

        Optional<Room> roomOpt = roomManager.getRoom(code);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Room not found or expired"));
        }

        Room room = roomOpt.get();
        return ResponseEntity.ok(Map.of(
                "roomCode", room.getRoomCode(),
                "participantCount", room.getParticipantCount(),
                "maxParticipants", 2
        ));
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
