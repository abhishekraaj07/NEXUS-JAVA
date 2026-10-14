package com.nexus.backend.util;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public final class RoomCodeGenerator {
    private static final char[] ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final int DEFAULT_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern CODE_PATTERN = Pattern.compile("^[0-9A-HJKMNP-TV-Z]{6,8}$");

    private RoomCodeGenerator() {}

    public static String generateRoomCode() {
        char[] chars = new char[DEFAULT_LENGTH];
        for (int i = 0; i < DEFAULT_LENGTH; i++) {
            chars[i] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(chars);
    }

    public static String normalize(String code) {
        if (code == null) return "";
        return code.trim()
                .toUpperCase()
                .replace("-", "")
                .replace(" ", "")
                .replace('I', '1')
                .replace('L', '1')
                .replace('O', '0');
    }

    public static boolean isValid(String code) {
        if (code == null) return false;
        String normalized = normalize(code);
        return CODE_PATTERN.matcher(normalized).matches();
    }
}
