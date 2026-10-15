package com.nexus.chat.crypto;

import android.util.Base64;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class NexusCrypto {
    public static final String PROTOCOL_CONTEXT = "NEXUS-E2EE-v1";
    private static final int AES_KEY_BITS = 256;
    private static final int IV_LENGTH_BYTES = 12; // 96 bits
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final KeyPair localKeyPair;
    private SecretKey sharedAesKey;
    private String sessionFingerprint;

    public NexusCrypto() throws GeneralSecurityException {
        // Generate ephemeral ECDH keypair using NIST P-256 (secp256r1)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        kpg.initialize(ecSpec, new SecureRandom());
        this.localKeyPair = kpg.generateKeyPair();
    }

    public Map<String, Object> exportPublicKeyJwk() {
        ECPublicKey pub = (ECPublicKey) localKeyPair.getPublic();
        ECPoint point = pub.getW();
        byte[] xBytes = toUnsigned32ByteArray(point.getAffineX());
        byte[] yBytes = toUnsigned32ByteArray(point.getAffineY());

        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "EC");
        jwk.put("crv", "P-256");
        jwk.put("x", base64UrlEncode(xBytes));
        jwk.put("y", base64UrlEncode(yBytes));
        return jwk;
    }

    public void establishSharedSecret(Map<String, Object> peerJwk) throws GeneralSecurityException {
        String crv = (String) peerJwk.get("crv");
        if (!"P-256".equals(crv)) {
            throw new IllegalArgumentException("Unsupported curve: " + crv);
        }

        byte[] xBytes = base64UrlDecode((String) peerJwk.get("x"));
        byte[] yBytes = base64UrlDecode((String) peerJwk.get("y"));

        ECPublicKey peerPublicKey = decodeP256PublicKey(xBytes, yBytes);

        // 1. ECDH Key Agreement
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(localKeyPair.getPrivate());
        ka.doPhase(peerPublicKey, true);
        byte[] rawSharedSecret = ka.generateSecret();

        // 2. HKDF-SHA-256 Key Derivation
        byte[] salt = new byte[16]; // RFC 5869 fixed 16-byte zero salt
        byte[] info = PROTOCOL_CONTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] derivedKeyBytes = hkdfExpand(hkdfExtract(salt, rawSharedSecret), info, 32);

        this.sharedAesKey = new SecretKeySpec(derivedKeyBytes, "AES");

        // 3. Deterministic Session Fingerprint (SHA-256 over canonically sorted keys)
        Map<String, Object> myJwk = exportPublicKeyJwk();
        String keyA = myJwk.get("x") + ":" + myJwk.get("y");
        String keyB = peerJwk.get("x") + ":" + peerJwk.get("y");
        String combined = keyA.compareTo(keyB) < 0 ? keyA + "|" + keyB : keyB + "|" + keyA;

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            hex.append(String.format("%02X", hash[i]));
        }
        // Format as XXXX-XXXX-XXXX
        this.sessionFingerprint = hex.substring(0, 4) + "-" + hex.substring(4, 8) + "-" + hex.substring(8, 12);
    }

    public EncryptedPayload encrypt(String plaintext, String roomCode, String senderId, String messageId) throws GeneralSecurityException {
        if (sharedAesKey == null) {
            throw new IllegalStateException("Shared secret not established");
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, sharedAesKey, spec);

        // Canonical AAD format matching Web Client & standard protocol
        String canonicalAad = roomCode + ":" + senderId + ":" + messageId;
        cipher.updateAAD(canonicalAad.getBytes(StandardCharsets.UTF_8));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        return new EncryptedPayload(
                Base64.encodeToString(ciphertext, Base64.NO_WRAP),
                Base64.encodeToString(iv, Base64.NO_WRAP)
        );
    }

    public String decrypt(String ciphertextB64, String ivB64, String roomCode, String senderId, String messageId) throws GeneralSecurityException {
        if (sharedAesKey == null) {
            throw new IllegalStateException("Shared secret not established");
        }

        byte[] iv = Base64.decode(ivB64, Base64.NO_WRAP);
        byte[] ciphertext = Base64.decode(ciphertextB64, Base64.NO_WRAP);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

        try {
            // Try standard canonical AAD: roomCode:senderId:messageId
            cipher.init(Cipher.DECRYPT_MODE, sharedAesKey, spec);
            String canonicalAad = roomCode + ":" + senderId + ":" + messageId;
            cipher.updateAAD(canonicalAad.getBytes(StandardCharsets.UTF_8));
            byte[] decrypted = cipher.doFinal(ciphertext);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException primaryEx) {
            // Fallback for legacy format
            cipher.init(Cipher.DECRYPT_MODE, sharedAesKey, spec);
            String fallbackAad = "NEXUS-v2|" + roomCode + "|" + senderId + "|" + messageId + "|0|message";
            cipher.updateAAD(fallbackAad.getBytes(StandardCharsets.UTF_8));
            byte[] decrypted = cipher.doFinal(ciphertext);
            return new String(decrypted, StandardCharsets.UTF_8);
        }
    }

    public String getSessionFingerprint() {
        return sessionFingerprint;
    }

    public boolean isEncrypted() {
        return sharedAesKey != null;
    }

    // --- Helper Utilities ---

    public static class EncryptedPayload {
        public final String ciphertext;
        public final String iv;
        public EncryptedPayload(String ciphertext, String iv) {
            this.ciphertext = ciphertext;
            this.iv = iv;
        }
    }

    private static ECPublicKey decodeP256PublicKey(byte[] x, byte[] y) throws GeneralSecurityException {
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec("secp256r1"));
        ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
        ECPoint point = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
        ECPublicKeySpec keySpec = new ECPublicKeySpec(point, ecParameterSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPublicKey) keyFactory.generatePublic(keySpec);
    }

    private static byte[] hkdfExtract(byte[] salt, byte[] ikm) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(salt, "HmacSHA256"));
        return mac.doFinal(ikm);
    }

    private static byte[] hkdfExpand(byte[] prk, byte[] info, int length) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(prk, "HmacSHA256"));
        byte[] okm = new byte[length];
        byte[] t = new byte[0];
        int offset = 0;
        byte counter = 1;

        while (offset < length) {
            mac.update(t);
            mac.update(info);
            mac.update(counter);
            t = mac.doFinal();
            int toCopy = Math.min(t.length, length - offset);
            System.arraycopy(t, 0, okm, offset, toCopy);
            offset += toCopy;
            counter++;
        }
        return okm;
    }

    private static byte[] toUnsigned32ByteArray(BigInteger bi) {
        byte[] raw = bi.toByteArray();
        if (raw.length == 32) return raw;
        byte[] out = new byte[32];
        if (raw.length > 32) {
            System.arraycopy(raw, raw.length - 32, out, 0, 32);
        } else {
            System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
        }
        return out;
    }

    private static String base64UrlEncode(byte[] b) {
        return Base64.encodeToString(b, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private static byte[] base64UrlDecode(String s) {
        return Base64.decode(s, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }
}
