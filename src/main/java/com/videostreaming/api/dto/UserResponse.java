package com.videostreaming.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.videostreaming.api.model.SubscriptionStatus;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String username;
    private String email;

    @JsonProperty("subscriptionStatus")
    private SubscriptionStatus subscriptionStatus;

    @JsonProperty("trialEndDate")
    private LocalDateTime trialEndDate;

    @JsonProperty("hasCreditCard")
    private boolean hasCreditCard;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, String email,
                        SubscriptionStatus subscriptionStatus, LocalDateTime trialEndDate,
                        boolean hasCreditCard, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.subscriptionStatus = subscriptionStatus;
        this.trialEndDate = trialEndDate;
        this.hasCreditCard = hasCreditCard;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public LocalDateTime getTrialEndDate() {
        return trialEndDate;
    }

    public void setTrialEndDate(LocalDateTime trialEndDate) {
        this.trialEndDate = trialEndDate;
    }

    public boolean isHasCreditCard() {
        return hasCreditCard;
    }

    public void setHasCreditCard(boolean hasCreditCard) {
        this.hasCreditCard = hasCreditCard;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}