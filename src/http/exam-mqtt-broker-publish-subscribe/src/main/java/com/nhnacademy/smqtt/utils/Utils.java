package com.nhnacademy.smqtt.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

public class Utils {
    static final Random random = new Random();

    Utils() {
    }

    public static String createClientID() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = messageDigest.digest(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte hb : hashBytes) {
                sb.append(String.format("%02x", hb));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ignore) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(String.format("%02x", (byte) random.nextInt()));
            }

            return sb.toString();
        }
    }
}
