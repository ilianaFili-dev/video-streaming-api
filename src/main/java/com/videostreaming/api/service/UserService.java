package com.videostreaming.api.service;


import com.videostreaming.api.dto.UserRegistrationRequest;
import com.videostreaming.api.dto.UserResponse;
import com.videostreaming.api.exception.*;
import com.videostreaming.api.model.SubscriptionStatus;
import com.videostreaming.api.model.User;
import com.videostreaming.api.repository.UserRepository;
import org.apache.commons.validator.routines.CreditCardValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Base64;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String hmacSecret;

    private static final int MINIMUM_AGE = 18;
    private static final int TRIAL_DAYS = 15;
    private static final CreditCardValidator CREDIT_CARD_VALIDATOR = new CreditCardValidator();

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.credit-card.hmac-secret}") String hmacSecret) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.hmacSecret = hmacSecret;
    }

    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Username '" + request.getUsername() + "' already exists"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email '" + request.getEmail() + "' is already registered"
            );
        }

        int age = calculateAge(request.getDateOfBirth());
        if (age < MINIMUM_AGE) {
            throw new InvalidAgeException(
                    "User must be at least " + MINIMUM_AGE + " years old"
            );
        }

        String creditCardHash = null;
        String creditCardLastFour = null;

        if (request.hasCreditCard()) {
            String creditCardNumber = request.getCreditCardNumber();

            if (!validateCreditCardLuhn(creditCardNumber)) {
                throw new InvalidValidationException(
                        "Invalid credit card number (Luhn check failed)"
                );
            }

            creditCardHash = hashCreditCard(creditCardNumber);
            if (userRepository.existsByCreditCardHash(creditCardHash)) {
                throw new UserAlreadyExistsException(
                        "Credit card is already registered to another user"
                );
            }

            creditCardLastFour = creditCardNumber.substring(creditCardNumber.length() - 4);
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getUsername(),
                hashedPassword,
                request.getEmail(),
                request.getDateOfBirth()
        );

        user.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        user.setTrialEndDate(LocalDateTime.now().plusDays(TRIAL_DAYS));

        if (creditCardHash != null) {
            user.setCreditCardHash(creditCardHash);
            user.setCreditCardLastFour(creditCardLastFour);
        }

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public void activateSubscription(User user, int durationDays) {
        user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        user.setSubscriptionEndDate(LocalDateTime.now().plusDays(durationDays));
        userRepository.save(user);
    }

    /**
     * Returns the HMAC-SHA256 hash of the given credit card number.
     * Used by PaymentService to look up a user by card hash.
     */
    public String computeCardHash(String creditCardNumber) {
        return hashCreditCard(creditCardNumber);
    }

    private int calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private boolean validateCreditCardLuhn(String creditCardNumber) {
        return CREDIT_CARD_VALIDATOR.isValid(creditCardNumber);
    }

    /**
     * Hashes the credit card number with HMAC-SHA256.
     * Deterministic: same card + same secret always produces the same hash,
     * enabling database lookup and duplicate detection.
     */
    private String hashCreditCard(String creditCardNumber) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(creditCardNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error hashing credit card number", e);
        }
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getSubscriptionStatus(),
                user.getTrialEndDate(),
                user.hasCreditCard(),
                user.getCreatedAt()
        );
    }
}