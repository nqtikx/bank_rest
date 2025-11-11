package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByOwnerIgnoreCaseAndStatusIn(String owner, CardStatus[] statuses, Pageable pageable);
    Page<Card> findByStatusIn(CardStatus[] statuses, Pageable pageable);

}
