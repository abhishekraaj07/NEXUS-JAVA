package com.nexus.backend;

import com.nexus.backend.service.RoomManager;
import com.nexus.backend.util.RoomCodeGenerator;
import com.nexus.backend.util.ValidationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NexusBackendApplicationTests {
    @Autowired
    private RoomManager roomManager;

    @Test
    void contextLoads() {
        assertNotNull(roomManager);
    }

    @Test
    void testRoomCodeGenerationAndValidation() {
        String code = RoomCodeGenerator.generateRoomCode();
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(RoomCodeGenerator.isValid(code));
    }

    @Test
    void testValidationUtilsSanitizeName() {
        assertEquals("Alice", ValidationUtils.sanitizeDisplayName("Alice\u0000\u0007"));
        assertEquals("Participant", ValidationUtils.sanitizeDisplayName("   "));
    }

    @Test
    void testPrototypePollutionCheck() {
        assertFalse(ValidationUtils.isSafeJson("{\"__proto__\":{\"admin\":true}}"));
        assertTrue(ValidationUtils.isSafeJson("{\"type\":\"ping\"}"));
    }
}
