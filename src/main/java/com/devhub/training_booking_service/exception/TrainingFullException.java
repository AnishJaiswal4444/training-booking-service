package com.devhub.training_booking_service.exception;

public class TrainingFullException extends RuntimeException {
    public TrainingFullException(String message) {
        super(message);
    }
}