package com.videostreaming.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videostreaming.api.config.SecurityConfig;
import com.videostreaming.api.dto.UserRegistrationRequest;
import com.videostreaming.api.dto.UserResponse;
import com.videostreaming.api.exception.InvalidAgeException;
import com.videostreaming.api.exception.UserAlreadyExistsException;
import com.videostreaming.api.model.SubscriptionStatus;
import com.videostreaming.api.model.User;
import com.videostreaming.api.repository.UserRepository;
import com.videostreaming.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private UserRegistrationRequest buildValidRequest() {
        return new UserRegistrationRequest(
                "user1",
                "Password123",
                "user1@example.com",
                LocalDate.of(1995, 6, 15),
                null
        );
    }

    private UserResponse buildUserResponse() {
        return new UserResponse(
                1L,
                "user1",
                "user1@example.com",
                SubscriptionStatus.TRIAL,
                LocalDateTime.now().plusDays(15),
                false,
                LocalDateTime.now()
        );
    }

    private User buildUserEntity(String username, boolean withCard) {
        User user = new User(username, "hashedPwd", username + "@example.com", LocalDate.of(1995, 6, 15));
        user.setId(1L);
        user.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        user.setCreatedAt(LocalDateTime.now());
        if (withCard) {
            user.setCreditCardHash("someHashValue");
            user.setCreditCardLastFour("0366");
        }
        return user;
    }

    @Test
    void registerUser_withValidData_returns201Created() throws Exception {
        UserRegistrationRequest request = buildValidRequest();
        UserResponse response = buildUserResponse();

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.subscriptionStatus").value("TRIAL"));
    }

    @Test
    void registerUser_withMissingRequiredField_returns400BadRequest() throws Exception {
        String requestJson = "{"
                + "\"password\": \"Password123\","
                + "\"email\": \"user1@example.com\","
                + "\"dateOfBirth\": \"1995-06-15\""
                + "}";

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_withInvalidEmailFormat_returns400BadRequest() throws Exception {
        String requestJson = "{"
                + "\"username\": \"user1\","
                + "\"password\": \"Password123\","
                + "\"email\": \"notAnEmail\","
                + "\"dateOfBirth\": \"1995-06-15\""
                + "}";

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void registerUser_withInvalidPasswordFormat_returns400BadRequest() throws Exception {
        String requestJson = "{"
                + "\"username\": \"user1\","
                + "\"password\": \"password\","
                + "\"email\": \"user1@example.com\","
                + "\"dateOfBirth\": \"1995-06-15\""
                + "}";

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void registerUser_withDuplicateUsername_returns409Conflict() throws Exception {
        UserRegistrationRequest request = buildValidRequest();

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username 'user1' already exists"));

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    void registerUser_withAgeUnder18_returns403Forbidden() throws Exception {
        UserRegistrationRequest request = buildValidRequest();

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new InvalidAgeException("User must be at least 18 years old"));

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403));
    }

    @Test
    void listUsers_withoutFilter_returns200WithAllUsers() throws Exception {
        List<User> allUsers = Arrays.asList(
                buildUserEntity("user1", true),
                buildUserEntity("user2", false)
        );
        when(userRepository.findAll()).thenReturn(allUsers);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.users").isArray());
    }

    @Test
    void listUsers_withCreditCardYesFilter_returns200WithFilteredUsers() throws Exception {
        List<User> usersWithCard = Arrays.asList(buildUserEntity("user1", true));
        when(userRepository.findAllWithCreditCard()).thenReturn(usersWithCard);

        mockMvc.perform(get("/api/users").param("creditCard", "Yes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void listUsers_withCreditCardNoFilter_returns200WithUsersWithoutCards() throws Exception {
        List<User> allUsers = Arrays.asList(
                buildUserEntity("user1", true),
                buildUserEntity("user2", false)
        );
        when(userRepository.findAll()).thenReturn(allUsers);

        mockMvc.perform(get("/api/users").param("creditCard", "No"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }
}