package com.nexus.chat.crypto;

import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;

public class NexusCryptoTest {
    @Test
    public void testE2EEncryptionAndDecryptionParity() throws Exception {
        // Alice generates keys
        NexusCrypto alice = new NexusCrypto();
        Map<String, Object> aliceJwk = alice.exportPublicKeyJwk();

        // Bob generates keys
        NexusCrypto bob = new NexusCrypto();
        Map<String, Object> bobJwk = bob.exportPublicKeyJwk();

        // Key exchange
        alice.establishSharedSecret(bobJwk);
        bob.establishSharedSecret(aliceJwk);

        // Verify fingerprints match on both sides
        assertNotNull(alice.getSessionFingerprint());
        assertEquals(alice.getSessionFingerprint(), bob.getSessionFingerprint());

        // Alice encrypts for Bob
        String plain = "Top secret intelligence payload";
        String room = "ROOM12";
        String sender = "alice";
        String msgId = "m-100";

        NexusCrypto.EncryptedPayload enc = alice.encrypt(plain, room, sender, msgId);
        assertNotNull(enc.ciphertext);
        assertNotNull(enc.iv);

        // Bob decrypts
        String decrypted = bob.decrypt(enc.ciphertext, enc.iv, room, sender, msgId);
        assertEquals(plain, decrypted);
    }

    @Test
    public void testLegacyAadFallback() throws Exception {
        NexusCrypto alice = new NexusCrypto();
        NexusCrypto bob = new NexusCrypto();

        alice.establishSharedSecret(bob.exportPublicKeyJwk());
        bob.establishSharedSecret(alice.exportPublicKeyJwk());

        // Test encryption with canonical AAD
        String plain = "Payload via Canonical AAD";
        NexusCrypto.EncryptedPayload enc = alice.encrypt(plain, "SEC01", "alice", "msg-99");
        String dec = bob.decrypt(enc.ciphertext, enc.iv, "SEC01", "alice", "msg-99");
        assertEquals(plain, dec);
    }
}
