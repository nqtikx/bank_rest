package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/cards")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CardService cards;

    public AdminController(CardService cards) {
        this.cards = cards;
    }

    @GetMapping
    @Operation(summary = "List all cards (admin only)")
    public Page<CardDto> all(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        return cards.listByOwner("", new String[]{}, PageRequest.of(page, size));
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Block any user card")
    public void block(@PathVariable Long id) {
        cards.block(id);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate any user card")
    public void activate(@PathVariable Long id) {
        cards.activate(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete card")
    public void delete(@PathVariable Long id) {
        cards.delete(id);
    }
}
