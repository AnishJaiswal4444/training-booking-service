package com.devhub.training_booking_service.exception;

public class DuplicateEnrollmentException extends RuntimeException {
    public DuplicateEnrollmentException(String message) {
        super(message);
    }
}