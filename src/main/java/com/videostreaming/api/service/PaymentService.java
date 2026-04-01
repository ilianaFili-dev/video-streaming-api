package com.videostreaming.api.service;

import com.videostreaming.api.dto.PaymentRequest;
import com.videostreaming.api.dto.PaymentResponse;
import com.videostreaming.api.exception.*;
import com.videostreaming.api.model.Payment;
import com.videostreaming.api.model.PaymentStatus;
import com.videostreaming.api.model.User;
import com.videostreaming.api.repository.PaymentRepository;
import com.videostreaming.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    private static final int SUBSCRIPTION_DURATION_DAYS = 30;

    public PaymentService(PaymentRepository paymentRepository,
                          UserService userService,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        String cardHash = userService.computeCardHash(request.getCreditCardNumber());
        Optional<User> foundUser = userRepository.findByCreditCardHash(cardHash);

        if (!foundUser.isPresent()) {
            throw new CreditCardNotFoundException(
                    "Credit card is not registered to any user"
            );
        }

        User user = foundUser.get();

        Payment payment = new Payment(
                user,
                request.getCreditCardNumber(),
                request.getAmount()
        );
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        userService.activateSubscription(user, SUBSCRIPTION_DURATION_DAYS);

        return mapToResponse(savedPayment, user);
    }

    private PaymentResponse mapToResponse(Payment payment, User user) {
        return new PaymentResponse(
                payment.getId(),
                user.getUsername(),
                payment.getAmount(),
                payment.getPaymentStatus(),
                user.getCreditCardLastFour(),
                payment.getCreatedAt()
        );
    }
}