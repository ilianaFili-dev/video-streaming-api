package com.videostreaming.api.controller;


import com.videostreaming.api.dto.UserRegistrationRequest;
import com.videostreaming.api.dto.UserResponse;
import com.videostreaming.api.model.User;
import com.videostreaming.api.repository.UserRepository;
import com.videostreaming.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listUsers(@RequestParam(required = false) String creditCard) {
        List<User> users;

        if (creditCard != null && !creditCard.isBlank()) {
            if (creditCard.equalsIgnoreCase("Yes")) {
                users = userRepository.findAllWithCreditCard();
            } else if (creditCard.equalsIgnoreCase("No")) {
                List<User> allUsers = userRepository.findAll();
                users = allUsers.stream()
                        .filter(user -> !user.hasCreditCard())
                        .toList();
            } else {
                users = userRepository.findAll();
            }
        } else {
            users = userRepository.findAll();
        }

        List<UserResponse> responses = users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getSubscriptionStatus(),
                        user.getTrialEndDate(),
                        user.hasCreditCard(),
                        user.getCreatedAt()
                ))
                .toList();

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("users", responses);
        responseMap.put("total", responses.size());

        return ResponseEntity.ok(responseMap);
    }
}