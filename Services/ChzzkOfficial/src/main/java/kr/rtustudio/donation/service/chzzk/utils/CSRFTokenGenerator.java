package kr.rtustudio.donation.service.chzzk.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.security.SecureRandom;
import java.util.Base64;

public class CSRFTokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder();

    public static @NotNull String generateDefault() {
        return generate(128);
    }

    public static @NotNull String generate(@Range(from = 32, to = Integer.MAX_VALUE) int length) {
        byte[] randomBytes = new byte[length];
        RANDOM.nextBytes(randomBytes);
        return ENCODER.encodeToString(randomBytes);
    }

}