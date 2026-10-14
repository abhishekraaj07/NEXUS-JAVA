package com.nexus.backend.util;

import java.util.Set;

public final class ValidationUtils {
    public static final int MAX_USERNAME_LENGTH = 32;
    public static final int MIN_USERNAME_LENGTH = 1;
    public static final int MAX_FRAME_SIZE = 8 * 1024 * 1024;
    public static final int MAX_CIPHERTEXT_SIZE = 6 * 1024 * 1024;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif",
            "application/pdf", "text/plain",
            "audio/webm", "audio/ogg", "audio/mp4", "audio/wav",
            "audio/aac", "audio/x-m4a", "audio/m4a"
    );

    private ValidationUtils() {}

    public static String sanitizeDisplayName(String name) {
        if (name == null) return "Participant";
        String cleaned = name.replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}]", "").trim();
        if (cleaned.isEmpty()) return "Participant";
        if (cleaned.length() > MAX_USERNAME_LENGTH) {
            return cleaned.substring(0, MAX_USERNAME_LENGTH);
        }
        return cleaned;
    }

    public static boolean isAllowedMimeType(String mime) {
        if (mime == null) return false;
        return ALLOWED_MIME_TYPES.contains(mime.trim().toLowerCase());
    }

    public static boolean isSafeJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return false;
        return !rawJson.contains("\"__proto__\"") &&
               !rawJson.contains("\"constructor\"") &&
               !rawJson.contains("\"prototype\"");
    }
}
