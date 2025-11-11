package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService service;

    public CardController(CardService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a new card for the authenticated user")
    public CardDto create(@RequestBody @Valid CreateCardRequest req) {
        return service.create(req);
    }

    @GetMapping
    @Operation(summary = "List all cards of the authenticated user")
    public Page<CardDto> list(
            @RequestParam(required = false) String[] statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.listByOwner(null, statuses, PageRequest.of(page, size));
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Block a card")
    public void block(@PathVariable Long id) {
        service.block(id);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a card")
    public void activate(@PathVariable Long id) {
        service.activate(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a card")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money between own cards")
    public TransferDto transfer(@RequestBody @Valid TransferRequest req) {
        return service.transfer(req);
    }
}
