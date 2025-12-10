package com.devhub.training_booking_service.repository;

import com.devhub.training_booking_service.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByUserIdAndTrainingId(Long userId, Long trainingId);

    List<Enrollment> findByUserIdAndStatus(Long userId, String status);

    boolean existsByUserIdAndTrainingId(Long userId, Long trainingId);
}
