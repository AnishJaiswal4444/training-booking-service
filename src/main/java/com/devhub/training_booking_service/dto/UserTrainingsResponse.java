package com.devhub.training_booking_service.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserTrainingsResponse {
    private Long userId;
    private String userName;
    private List<EnrollmentInfo> trainings;
    private Integer totalEnrollments;

    @Data
    public static class EnrollmentInfo {
        private Long enrollmentId;
        private LocalDateTime enrolledAt;
        private String status;
        private TrainingDetails training;
    }

    @Data
    public static class TrainingDetails {
        private Long id;
        private String title;
        private String description;
        private String instructor;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}