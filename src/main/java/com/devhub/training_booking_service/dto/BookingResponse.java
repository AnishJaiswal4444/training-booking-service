package com.devhub.training_booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingResponse {
    private String message;
    private Long enrollmentId;
    private TrainingInfo training;

    @Data
    @AllArgsConstructor
    public static class TrainingInfo {
        private Long id;
        private String title;
        private LocalDateTime startDate;
    }
}