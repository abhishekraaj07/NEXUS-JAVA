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
}
