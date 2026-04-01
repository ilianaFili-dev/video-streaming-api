package com.videostreaming.api.service;

import com.videostreaming.api.dto.UserRegistrationRequest;
import com.videostreaming.api.dto.UserResponse;
import com.videostreaming.api.exception.InvalidAgeException;
import com.videostreaming.api.exception.InvalidValidationException;
import com.videostreaming.api.exception.UserAlreadyExistsException;
import com.videostreaming.api.model.SubscriptionStatus;
import com.videostreaming.api.model.User;
import com.videostreaming.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private static final String HMAC_SECRET = "test-secret-key-for-hmac-sha256-unit-tests!";

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, HMAC_SECRET);
    }

    private UserRegistrationRequest buildRequestWithCard() {
        return new UserRegistrationRequest(
                "user1",
                "Password123",
                "user1@example.com",
                LocalDate.of(1995, 6, 15),
                "4532015112830366"
        );
    }

    private UserRegistrationRequest buildRequestWithoutCard() {
        return new UserRegistrationRequest(
                "user1",
                "Password123",
                "user1@example.com",
                LocalDate.of(1995, 6, 15),
                null
        );
    }

    private User buildSavedUser(boolean withCard) {
        User user = new User("user1", "hashedPassword", "user1@example.com", LocalDate.of(1995, 6, 15));
        user.setId(1L);
        user.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        user.setTrialEndDate(LocalDateTime.now().plusDays(15));
        user.setCreatedAt(LocalDateTime.now());
        if (withCard) {
            user.setCreditCardHash("hmacHashValue");
            user.setCreditCardLastFour("0366");
        }
        return user;
    }

    @Test
    void registerUser_withValidDataAndCreditCard_succeeds() {
        UserRegistrationRequest request = buildRequestWithCard();

        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);
        when(userRepository.existsByCreditCardHash(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(buildSavedUser(true));

        UserResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals("user1", response.getUsername());
        assertEquals(SubscriptionStatus.TRIAL, response.getSubscriptionStatus());
        assertTrue(response.isHasCreditCard());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_withValidDataWithoutCreditCard_succeeds() {
        UserRegistrationRequest request = buildRequestWithoutCard();

        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(buildSavedUser(false));

        UserResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertFalse(response.isHasCreditCard());
        verify(userRepository, never()).existsByCreditCardHash(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_duplicateUsername_throwsUserAlreadyExistsException() {
        UserRegistrationRequest request = buildRequestWithoutCard();
        when(userRepository.existsByUsername("user1")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, new Executable() {
            @Override
            public void execute() {
                userService.registerUser(request);
            }
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_duplicateEmail_throwsUserAlreadyExistsException() {
        UserRegistrationRequest request = buildRequestWithoutCard();
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, new Executable() {
            @Override
            public void execute() {
                userService.registerUser(request);
            }
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_duplicateCreditCard_throwsUserAlreadyExistsException() {
        UserRegistrationRequest request = buildRequestWithCard();
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);
        when(userRepository.existsByCreditCardHash(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, new Executable() {
            @Override
            public void execute() {
                userService.registerUser(request);
            }
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_userUnder18Years_throwsInvalidAgeException() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "younguser",
                "Password123",
                "young@example.com",
                LocalDate.now().minusYears(10),
                null
        );
        when(userRepository.existsByUsername("younguser")).thenReturn(false);
        when(userRepository.existsByEmail("young@example.com")).thenReturn(false);

        assertThrows(InvalidAgeException.class, new Executable() {
            @Override
            public void execute() {
                userService.registerUser(request);
            }
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_invalidLuhnCreditCard_throwsInvalidValidationException() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "user1",
                "Password123",
                "user1@example.com",
                LocalDate.of(1995, 6, 15),
                "1234567890123456"
        );
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);

        assertThrows(InvalidValidationException.class, new Executable() {
            @Override
            public void execute() {
                userService.registerUser(request);
            }
        });
        verify(userRepository, never()).save(any(User.class));
    }
}