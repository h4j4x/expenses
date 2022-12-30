package com.h4j4x.expenses.api.security.impl;

import com.h4j4x.expenses.api.security.StringHasher;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class PBKDF2StringHasher implements StringHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String SEPARATOR = ":";
    private static final int RADIX = 16;

    @ConfigProperty(name = "app.security.hash-iterations", defaultValue = "512")
    Integer iterations = 512;

    @ConfigProperty(name = "app.security.hash-key-length", defaultValue = "128")
    Integer keyLength = 128;

    @Override
    public String salt() {
        SecureRandom random = new SecureRandom();
        var salt = new byte[16];
        random.nextBytes(salt);
        return toHex(salt);
    }

    @Override
    public String hash(String string, String salt) throws GeneralSecurityException {
        var saltBytes = fromHex(salt);
        var spec = new PBEKeySpec(string.toCharArray(), saltBytes, iterations, keyLength);
        var factory = SecretKeyFactory.getInstance(ALGORITHM);
        var hash = factory.generateSecret(spec).getEncoded();
        return iterations + SEPARATOR + toHex(hash);
    }

    private String toHex(byte[] array) {
        var bi = new BigInteger(1, array);
        var hex = bi.toString(RADIX);
        var paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        }
        return hex;
    }

    @Override
    public boolean match(String string, String salt, String hash) throws GeneralSecurityException {
        if (string == null || hash == null) {
            return false;
        }
        var parts = hash.split(SEPARATOR);
        if (parts.length != 2) {
            return false;
        }
        var iterations = Integer.parseInt(parts[0]);
        var hashBytes = fromHex(parts[1]);
        var saltBytes = fromHex(salt);
        var spec = new PBEKeySpec(string.toCharArray(), saltBytes, iterations, hashBytes.length * 8);
        var skf = SecretKeyFactory.getInstance(ALGORITHM);
        var testHash = skf.generateSecret(spec).getEncoded();

        var diff = hashBytes.length ^ testHash.length;
        for (int i = 0; i < hashBytes.length && i < testHash.length; i++) {
            diff |= hashBytes[i] ^ testHash[i];
        }
        return diff == 0;
    }

    private byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), RADIX);
        }
        return bytes;
    }
}
