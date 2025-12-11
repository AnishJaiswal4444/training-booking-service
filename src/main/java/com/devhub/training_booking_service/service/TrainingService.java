package com.devhub.training_booking_service.service;

import com.devhub.training_booking_service.dto.BookingResponse;
import com.devhub.training_booking_service.dto.UserTrainingsResponse;
import com.devhub.training_booking_service.exception.DuplicateEnrollmentException;
import com.devhub.training_booking_service.exception.ResourceNotFoundException;
import com.devhub.training_booking_service.exception.TrainingFullException;
import com.devhub.training_booking_service.model.Enrollment;
import com.devhub.training_booking_service.model.Training;
import com.devhub.training_booking_service.model.User;
import com.devhub.training_booking_service.repository.EnrollmentRepository;
import com.devhub.training_booking_service.repository.TrainingRepository;
import com.devhub.training_booking_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public BookingResponse bookTraining(Long trainingId, Long userId) {
        log.info("Attempting to book training {} for user {}", trainingId, userId);

        // 1. Validate training exists
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Training not found with id: " + trainingId));

        // 2. Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        // 3. Check if already enrolled (business rule: one booking per user per training)
        if (enrollmentRepository.existsByUserIdAndTrainingId(userId, trainingId)) {
            log.warn("User {} already enrolled in training {}", userId, trainingId);
            throw new DuplicateEnrollmentException(
                    "User is already enrolled in this training");
        }

        // 4. Check capacity (business rule: respect max capacity)
        if (training.getCurrentEnrollment() >= training.getMaxCapacity()) {
            log.warn("Training {} is full (capacity: {})", trainingId, training.getMaxCapacity());
            throw new TrainingFullException(
                    "Training has reached maximum capacity");
        }

        // 5. Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setTraining(training);
        enrollment.setStatus("ACTIVE");

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Created enrollment {} for user {} in training {}",
                savedEnrollment.getId(), userId, trainingId);

        // 6. Update training enrollment count (atomic operation within transaction)
        training.setCurrentEnrollment(training.getCurrentEnrollment() + 1);
        trainingRepository.save(training);

        log.info("Successfully booked training {} for user {}. Current enrollment: {}/{}",
                trainingId, userId, training.getCurrentEnrollment(), training.getMaxCapacity());

        // 7. Return response
        BookingResponse.TrainingInfo trainingInfo = new BookingResponse.TrainingInfo(
                training.getId(),
                training.getTitle(),
                training.getStartDate()
        );

        return new BookingResponse(
                "Successfully enrolled in training",
                savedEnrollment.getId(),
                trainingInfo
        );
    }

    @Transactional(readOnly = true)
    public UserTrainingsResponse getUserTrainings(Long userId) {
        log.info("Fetching trainings for user {}", userId);

        // 1. Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        // 2. Get all active enrollments
        List<Enrollment> enrollments = enrollmentRepository
                .findByUserIdAndStatus(userId, "ACTIVE");

        log.info("Found {} active enrollments for user {}", enrollments.size(), userId);

        // 3. Map to response DTO
        List<UserTrainingsResponse.EnrollmentInfo> enrollmentInfos = enrollments.stream()
                .map(this::mapToEnrollmentInfo)
                .collect(Collectors.toList());

        UserTrainingsResponse response = new UserTrainingsResponse();
        response.setUserId(userId);
        response.setUserName(user.getName());
        response.setTrainings(enrollmentInfos);
        response.setTotalEnrollments(enrollmentInfos.size());

        return response;
    }

    private UserTrainingsResponse.EnrollmentInfo mapToEnrollmentInfo(Enrollment enrollment) {
        UserTrainingsResponse.EnrollmentInfo info = new UserTrainingsResponse.EnrollmentInfo();
        info.setEnrollmentId(enrollment.getId());
        info.setEnrolledAt(enrollment.getEnrolledAt());
        info.setStatus(enrollment.getStatus());

        Training training = enrollment.getTraining();
        UserTrainingsResponse.TrainingDetails details = new UserTrainingsResponse.TrainingDetails();
        details.setId(training.getId());
        details.setTitle(training.getTitle());
        details.setDescription(training.getDescription());
        details.setInstructor(training.getInstructor());
        details.setStartDate(training.getStartDate());
        details.setEndDate(training.getEndDate());

        info.setTraining(details);
        return info;
    }
}