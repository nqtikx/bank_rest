package com.example.bankcards.util;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardMask;

public final class CardMapper {
    private CardMapper() {}

    public static CardDto toDto(Card c, String decryptedPan) {
        return new CardDto(
                c.getId(),
                CardMask.maskPan(decryptedPan),
                c.getOwner(),
                c.getExpiry(),
                c.getStatus(),
                c.getBalance()
        );
    }
}
