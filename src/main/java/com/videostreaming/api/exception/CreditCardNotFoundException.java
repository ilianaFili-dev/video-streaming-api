package com.videostreaming.api.exception;

public class CreditCardNotFoundException extends RuntimeException {

    public CreditCardNotFoundException(String message) {
        super(message);
    }
}