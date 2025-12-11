package com.devhub.training_booking_service.controller;

import com.devhub.training_booking_service.dto.BookingResponse;
import com.devhub.training_booking_service.dto.UserTrainingsResponse;
import com.devhub.training_booking_service.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping("/trainings/{trainingId}/book")
    public ResponseEntity<BookingResponse> bookTraining(
            @PathVariable Long trainingId,
            @RequestParam Long userId) {

        BookingResponse response = trainingService.bookTraining(trainingId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/users/{userId}/trainings")
    public ResponseEntity<UserTrainingsResponse> getUserTrainings(
            @PathVariable Long userId) {

        UserTrainingsResponse response = trainingService.getUserTrainings(userId);
        return ResponseEntity.ok(response);
    }
}