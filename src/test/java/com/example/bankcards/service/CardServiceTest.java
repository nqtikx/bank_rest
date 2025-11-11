package com.example.bankcards.service;

import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.CardTransfer;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardTransferRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.CryptoConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepo;

    @Mock
    private CardTransferRepository transferRepo;

    @InjectMocks
    private CardServiceImpl service;

    @Captor
    private ArgumentCaptor<Card> cardCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        var auth = new UsernamePasswordAuthenticationToken("user1", null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("create() — создаёт карту и шифрует PAN")
    void createCard_success() {
        var req = new CreateCardRequest("4111111111111111", LocalDate.now().plusYears(2));
        var saved = new Card();
        saved.setId(1L);
        saved.setOwner("user1");
        saved.setPanEncrypted(new CryptoConverter().convertToDatabaseColumn(req.pan()));
        saved.setStatus(CardStatus.ACTIVE);
        saved.setExpiry(req.expiry());
        saved.setBalance(BigDecimal.ZERO);

        when(cardRepo.save(any(Card.class))).thenReturn(saved);

        var result = service.create(req);

        assertThat(result.owner()).isEqualTo("user1");
        assertThat(result.maskedNumber()).endsWith("1111");
        assertThat(result.status()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepo, times(1)).save(cardCaptor.capture());
    }

    @Test
    @DisplayName("transfer() — успешный перевод между своими активными картами")
    void transfer_success() {
        // given
        var from = new Card();
        from.setId(1L);
        from.setOwner("user1");
        from.setStatus(CardStatus.ACTIVE);
        from.setBalance(new BigDecimal("1000.00"));

        var to = new Card();
        to.setId(2L);
        to.setOwner("user1");
        to.setStatus(CardStatus.ACTIVE);
        to.setBalance(new BigDecimal("0.00"));

        when(cardRepo.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepo.findById(2L)).thenReturn(Optional.of(to));

        var transfer = new CardTransfer();
        transfer.setId(99L);
        transfer.setOwner("user1");
        transfer.setFromCardId(1L);
        transfer.setToCardId(2L);
        transfer.setAmount(new BigDecimal("250.00"));
        transfer.setCreatedAt(java.time.LocalDateTime.now());

        when(transferRepo.save(any(CardTransfer.class))).thenReturn(transfer);

        var req = new TransferRequest(1L, 2L, new BigDecimal("250.00"));
        var result = service.transfer("user1", req);

        assertThat(result.amount()).isEqualByComparingTo("250.00");
        assertThat(from.getBalance()).isEqualByComparingTo("750.00");
        assertThat(to.getBalance()).isEqualByComparingTo("250.00");
        verify(cardRepo, times(2)).save(any(Card.class));
        verify(transferRepo).save(any(CardTransfer.class));
    }

    @Test
    @DisplayName("transfer() — выбрасывает ошибку при недостатке средств")
    void transfer_insufficientFunds() {
        var from = new Card();
        from.setId(1L);
        from.setOwner("user1");
        from.setStatus(CardStatus.ACTIVE);
        from.setBalance(new BigDecimal("10.00"));

        var to = new Card();
        to.setId(2L);
        to.setOwner("user1");
        to.setStatus(CardStatus.ACTIVE);
        to.setBalance(new BigDecimal("0.00"));

        when(cardRepo.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepo.findById(2L)).thenReturn(Optional.of(to));

        var req = new TransferRequest(1L, 2L, new BigDecimal("50.00"));

        assertThatThrownBy(() -> service.transfer("user1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    @DisplayName("transfer() — выбрасывает ошибку при попытке перевода самому себе")
    void transfer_sameCard() {
        var req = new TransferRequest(1L, 1L, new BigDecimal("100.00"));
        assertThatThrownBy(() -> service.transfer("user1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("from == to");
    }
}
