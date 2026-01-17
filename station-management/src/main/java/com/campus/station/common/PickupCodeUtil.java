package com.campus.station.common;

import java.security.SecureRandom;

public final class PickupCodeUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PickupCodeUtil() {
    }

    public static String generate() {
        int value = RANDOM.nextInt(900000) + 100000;
        return Integer.toString(value);
    }
}

