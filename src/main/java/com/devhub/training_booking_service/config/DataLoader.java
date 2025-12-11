package com.devhub.training_booking_service.config;

import com.devhub.training_booking_service.model.Training;
import com.devhub.training_booking_service.model.User;
import com.devhub.training_booking_service.repository.TrainingRepository;
import com.devhub.training_booking_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TrainingRepository trainingRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create test users
        User user1 = new User();
        user1.setName("Hardik Jaiswal");
        user1.setEmail("hardik@gradguide.com");
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("John Doe");
        user2.setEmail("john@example.com");
        userRepository.save(user2);

        User user3 = new User();
        user3.setName("Jane Smith");
        user3.setEmail("jane@example.com");
        userRepository.save(user3);

        // Create test trainings
        Training training1 = new Training();
        training1.setTitle("Spring Boot Masterclass");
        training1.setDescription("Learn Spring Boot from scratch with hands-on projects");
        training1.setInstructor("John Doe");
        training1.setStartDate(LocalDateTime.now().plusDays(10));
        training1.setEndDate(LocalDateTime.now().plusDays(12));
        training1.setMaxCapacity(30);
        training1.setCurrentEnrollment(0);
        trainingRepository.save(training1);

        Training training2 = new Training();
        training2.setTitle("React Advanced Patterns");
        training2.setDescription("Master React hooks, context, and advanced patterns");
        training2.setInstructor("Jane Smith");
        training2.setStartDate(LocalDateTime.now().plusDays(15));
        training2.setEndDate(LocalDateTime.now().plusDays(17));
        training2.setMaxCapacity(25);
        training2.setCurrentEnrollment(0);
        trainingRepository.save(training2);

        Training training3 = new Training();
        training3.setTitle("Docker & Kubernetes");
        training3.setDescription("Container orchestration and deployment strategies");
        training3.setInstructor("Mike Johnson");
        training3.setStartDate(LocalDateTime.now().plusDays(20));
        training3.setEndDate(LocalDateTime.now().plusDays(22));
        training3.setMaxCapacity(20);
        training3.setCurrentEnrollment(0);
        trainingRepository.save(training3);

        Training training4 = new Training();
        training4.setTitle("Microservices Architecture");
        training4.setDescription("Design and build scalable microservices");
        training4.setInstructor("Sarah Williams");
        training4.setStartDate(LocalDateTime.now().plusDays(25));
        training4.setEndDate(LocalDateTime.now().plusDays(27));
        training4.setMaxCapacity(15);
        training4.setCurrentEnrollment(0);
        trainingRepository.save(training4);

        System.out.println("✅ Test data loaded successfully!");
        System.out.println("📚 Created 3 users and 4 trainings");
    }
}