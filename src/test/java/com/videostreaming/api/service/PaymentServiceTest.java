package com.videostreaming.api.service;

import com.videostreaming.api.dto.PaymentRequest;
import com.videostreaming.api.dto.PaymentResponse;
import com.videostreaming.api.exception.CreditCardNotFoundException;
import com.videostreaming.api.model.Payment;
import com.videostreaming.api.model.PaymentStatus;
import com.videostreaming.api.model.SubscriptionStatus;
import com.videostreaming.api.model.User;
import com.videostreaming.api.repository.PaymentRepository;
import com.videostreaming.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, userService, userRepository);
    }

    private User buildUserWithCard() {
        User user = new User("user1", "hashedPassword", "user1@example.com", LocalDate.of(1995, 6, 15));
        user.setId(1L);
        user.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        user.setCreditCardHash("hmacHashValue");
        user.setCreditCardLastFour("0366");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Test
    void processPayment_withValidCardAndAmount_succeeds() {
        PaymentRequest request = new PaymentRequest("4532015112830366", new BigDecimal("99.99"));
        User user = buildUserWithCard();

        when(userService.computeCardHash("4532015112830366")).thenReturn("hmacHashValue");
        when(userRepository.findByCreditCardHash("hmacHashValue")).thenReturn(Optional.of(user));

        Payment savedPayment = new Payment(user, "4532015112830366", new BigDecimal("99.99"));
        savedPayment.setId(1L);
        savedPayment.setPaymentStatus(PaymentStatus.SUCCESS);
        savedPayment.setCreatedAt(LocalDateTime.now());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals("0366", response.getCreditCardLastFour());
        assertEquals(new BigDecimal("99.99"), response.getAmount());
        verify(userService, times(1)).activateSubscription(eq(user), anyInt());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void processPayment_withUnregisteredCard_throwsCreditCardNotFoundException() {
        PaymentRequest request = new PaymentRequest("5105105105105100", new BigDecimal("99.99"));

        when(userService.computeCardHash("5105105105105100")).thenReturn("unregisteredHash");
        when(userRepository.findByCreditCardHash("unregisteredHash")).thenReturn(Optional.empty());

        assertThrows(CreditCardNotFoundException.class, new Executable() {
            @Override
            public void execute() {
                paymentService.processPayment(request);
            }
        });
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(userService, never()).activateSubscription(any(User.class), anyInt());
    }
}