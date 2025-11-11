package com.example.bankcards.util;

public final class CardMask {
    private CardMask() {}

    public static String maskPan(String pan) {
        if (pan == null || pan.length() < 4) return "****";
        var last4 = pan.substring(pan.length() - 4);
        return "**** **** **** " + last4;
    }
}
