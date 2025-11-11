package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;


    @DisplayName("POST /api/v1/cards — создаёт карту и возвращает DTO")
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void createCard_shouldReturnCardDto() throws Exception {

        var dto = new CardDto(
                1L,
                "**** **** **** 1111",
                "user1",
                LocalDate.of(2027, 12, 31),
                CardStatus.ACTIVE,
                BigDecimal.ZERO
        );

        Mockito.when(cardService.create(any(CreateCardRequest.class)))
                .thenReturn(dto);

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pan":"4111111111111111","expiry":"2027-12-31"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 1111"))
                .andExpect(jsonPath("$.owner").value("user1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }


    @Test
    @DisplayName("PATCH /api/v1/cards/{id}/block — блокирует карту")
    @WithMockUser(username = "user1", roles = {"USER"})
    void blockCard_shouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/v1/cards/1/block"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/cards/{id} — удаляет карту")
    @WithMockUser(username = "user1", roles = {"USER"})
    void deleteCard_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/cards/1"))
                .andExpect(status().isOk());
    }
}
