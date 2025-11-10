package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {
    private final CardService service;

    public CardController(CardService service) { this.service = service; }

    @PostMapping
    public CardDto create(@RequestBody @Valid CreateCardRequest req) {
        return service.create(req);
    }

    @GetMapping
    public Page<CardDto> listByOwner(
            @RequestParam String owner,
            @RequestParam(required = false) String[] statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.listByOwner(owner, statuses, PageRequest.of(page, size));
    }
}
