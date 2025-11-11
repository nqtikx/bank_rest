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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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
    @PreAuthorize("hasRole('ADMIN')") // создавать карты — только ADMIN
    @Operation(summary = "Create a new card for the authenticated user")
    public ResponseEntity<CardDto> create(@AuthenticationPrincipal User user,
                                          @RequestBody CreateCardRequest req) {
        var created = service.create(req);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "List all cards of the authenticated user")
    public Page<CardDto> list(
            @RequestParam(name = "statuses", required = false) String[] statuses,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return service.listByOwner(null, statuses, PageRequest.of(page, size));
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block a card")
    public void block(@PathVariable Long id) {
        service.block(id);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a card")
    public void activate(@PathVariable Long id) {
        service.activate(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a card")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Transfer money between own cards")
    public TransferDto transfer(@RequestBody @Valid TransferRequest req,
                                @AuthenticationPrincipal UserDetails me) {
        return service.transfer(me.getUsername(), req);
    }
}
