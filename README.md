# Training/Lab Booking System

A RESTful API for managing training bookings with proper transaction handling, business rule enforcement, and centralized exception handling.

## 🚀 Features

- Book trainings with duplicate prevention
- View user's enrolled trainings
- Automatic capacity management
- Transaction-based consistency
- Centralized exception handling with `@ControllerAdvice`
- Comprehensive logging
- H2 in-memory database for quick setup

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

## 🛠️ Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/AnishJaiswal4444/training-booking-service.git
cd training-booking-service
```

### 2. Build the Project
```bash
./mvnw clean install
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access H2 Console (Optional)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:trainingdb`
- Username: `sa`
- Password: (leave blank)

## 📡 API Endpoints

### 1. Book a Training
```http
POST /api/trainings/{trainingId}/book?userId={userId}
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/trainings/1/book?userId=1"
```

**Success Response (201 Created):**
```json
{
  "message": "Successfully enrolled in training",
  "enrollmentId": 1,
  "training": {
    "id": 1,
    "title": "Spring Boot Masterclass",
    "startDate": "2024-12-21T10:00:00"
  }
}
```

**Error Responses:**
- `404 Not Found` - Training or User doesn't exist
- `409 Conflict` - User already enrolled in this training
- `400 Bad Request` - Training has reached maximum capacity

---

### 2. Get User's Trainings
```http
GET /api/users/{userId}/trainings
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/users/1/trainings"
```

**Success Response (200 OK):**
```json
{
  "userId": 1,
  "userName": "Hardik Jaiswal",
  "trainings": [
    {
      "enrollmentId": 1,
      "enrolledAt": "2024-12-11T15:30:00",
      "status": "ACTIVE",
      "training": {
        "id": 1,
        "title": "Spring Boot Masterclass",
        "description": "Learn Spring Boot from scratch with hands-on projects",
        "instructor": "John Doe",
        "startDate": "2024-12-21T10:00:00",
        "endDate": "2024-12-23T18:00:00"
      }
    }
  ],
  "totalEnrollments": 1
}
```

**Error Response:**
- `404 Not Found` - User doesn't exist

## 🏗️ Key Design Decisions

### 1. **Database Design**
- **Three-Entity Model**: `User`, `Training`, `Enrollment`
- **Composite Unique Constraint**: `@UniqueConstraint(columnNames = {"user_id", "training_id"})` prevents duplicate bookings at database level
- **Enrollment as Bridge Entity**: Tracks booking metadata (timestamp, status) and enables many-to-many relationship

**Rationale:** This design ensures data integrity at the database layer while maintaining flexibility for future enhancements like booking history and cancellations.

---

### 2. **Transaction Management**
- `@Transactional` annotation ensures **atomicity** of operations
- Both enrollment creation and capacity increment succeed together or fail together
- Prevents race conditions in concurrent booking scenarios

**Example:**
```java
@Transactional
public BookingResponse bookTraining(Long trainingId, Long userId) {
    // Create enrollment + Update capacity (atomic)
}
```

**Rationale:** Guarantees data consistency even under high concurrency. If enrollment creation succeeds but capacity update fails, the entire transaction rolls back.

---

### 3. **Centralized Exception Handling**
- **`@RestControllerAdvice`** provides single point of error handling
- **Custom Exception Classes** for clear business logic violations:
    - `ResourceNotFoundException` → 404 Not Found
    - `DuplicateEnrollmentException` → 409 Conflict
    - `TrainingFullException` → 400 Bad Request
- **Consistent Error Response Structure**:
```json
  {
    "status": 409,
    "message": "User is already enrolled in this training",
    "path": "uri=/api/trainings/1/book",
    "timestamp": "2024-12-11T15:30:00"
  }
```

**Rationale:**
- Eliminates repetitive try-catch blocks in controllers
- Provides uniform error responses across all endpoints
- Easier to maintain and extend error handling logic

---

### 4. **RESTful API Design**
- Proper HTTP verbs: `POST` for creation, `GET` for retrieval
- Meaningful status codes: `201 Created`, `200 OK`, `404 Not Found`, `409 Conflict`
- Resource-based URLs: `/trainings/{id}/book`, `/users/{id}/trainings`

**Rationale:** Follows REST best practices, making the API intuitive and predictable for consumers.

---

### 5. **Data Consistency Rules**

#### **Rule 1: One Booking Per User Per Training**
- **Enforced at two levels:**
    1. Database: `UNIQUE` constraint on `(user_id, training_id)`
    2. Application: Business logic check before creating enrollment

**Code:**
```java
if (enrollmentRepository.existsByUserIdAndTrainingId(userId, trainingId)) {
    throw new DuplicateEnrollmentException("User is already enrolled");
}
```

#### **Rule 2: Capacity Management**
- Atomic increment of `currentEnrollment` within transaction
- Capacity check before enrollment creation
- Prevents overbooking even in concurrent scenarios

**Rationale:** Dual-layer enforcement provides defense in depth. Database constraint is the final safeguard if application logic fails.

---

### 6. **Layered Architecture**
```
┌─────────────────────────────────────┐
│   Controller Layer                  │  ← HTTP Request/Response
├─────────────────────────────────────┤
│   Service Layer                     │  ← Business Logic + Transactions
├─────────────────────────────────────┤
│   Repository Layer                  │  ← Data Access (JPA)
├─────────────────────────────────────┤
│   Model Layer                       │  ← Entities + DTOs
└─────────────────────────────────────┘
```

**Benefits:**
- Clear separation of concerns
- Easy to test each layer independently
- Flexible to change implementation (e.g., switch from H2 to MySQL)

---

### 7. **DTO Pattern**
- Separate DTOs (`BookingResponse`, `UserTrainingsResponse`) for API responses
- **Benefits:**
    - Decouples API contract from database schema
    - Prevents exposing internal entity structure
    - Allows API evolution without breaking database changes

**Example:** Can add/remove fields in Training entity without affecting API response structure.

---

### 8. **Logging Strategy**
- SLF4J with Lombok's `@Slf4j` for clean logging
- Logs important business events:
    - Booking attempts
    - Capacity warnings
    - Enrollment success/failure

**Example:**
```java
log.info("Successfully booked training {} for user {}. Current: {}/{}", 
    trainingId, userId, currentEnrollment, maxCapacity);
```

**Rationale:** Provides audit trail and debugging information without cluttering code with `System.out.println`.

---

### 9. **H2 In-Memory Database**
- Zero external dependencies for development/demo
- Auto-creates schema on startup (`ddl-auto=create-drop`)
- Preloaded test data via `CommandLineRunner`

**Rationale:** Enables quick setup and testing. Can easily switch to MySQL/PostgreSQL by changing `application.properties`.

---

### 10. **Idempotency Consideration**
- Duplicate booking attempts return `409 Conflict` (not 500 error)
- Safe to retry failed requests
- No side effects from repeated identical requests

**Rationale:** Improves API reliability and makes it safe for clients to implement retry logic.

---

## 🧪 Testing Scenarios

### Scenario 1: Successful Booking
```bash
curl -X POST "http://localhost:8080/api/trainings/1/book?userId=1"
# ✅ Returns 201 with enrollment details
```

### Scenario 2: Duplicate Booking Prevention
```bash
curl -X POST "http://localhost:8080/api/trainings/1/book?userId=1"
curl -X POST "http://localhost:8080/api/trainings/1/book?userId=1"
# ✅ Second request returns 409 Conflict
```

### Scenario 3: Capacity Enforcement
```bash
# Book training until capacity reached (assuming capacity = 30)
for i in {1..30}; do
  curl -X POST "http://localhost:8080/api/trainings/1/book?userId=$i"
done
# Next booking should fail
curl -X POST "http://localhost:8080/api/trainings/1/book?userId=31"
# ✅ Returns 400 Bad Request - Training Full
```

### Scenario 4: Invalid Resources
```bash
curl -X POST "http://localhost:8080/api/trainings/999/book?userId=1"
# ✅ Returns 404 - Training not found

curl -X POST "http://localhost:8080/api/trainings/1/book?userId=999"
# ✅ Returns 404 - User not found
```

### Scenario 5: Retrieve User Trainings
```bash
curl -X GET "http://localhost:8080/api/users/1/trainings"
# ✅ Returns list of all enrolled trainings
```

---

## 📁 Project Structure
```
src/main/java/com/devhub/training_booking_service/
├── config/
│   └── DataLoader.java                 # Test data initialization
├── controller/
│   └── TrainingController.java         # REST endpoints
├── dto/
│   ├── BookingResponse.java            # Booking response DTO
│   └── UserTrainingsResponse.java      # User trainings response DTO
├── exception/
│   ├── GlobalExceptionHandler.java     # Centralized error handling
│   ├── ResourceNotFoundException.java  # 404 errors
│   ├── DuplicateEnrollmentException.java  # 409 conflicts
│   └── TrainingFullException.java      # 400 capacity errors
├── model/
│   ├── User.java                       # User entity
│   ├── Training.java                   # Training entity
│   └── Enrollment.java                 # Enrollment entity (bridge table)
├── repository/
│   ├── UserRepository.java             # User data access
│   ├── TrainingRepository.java         # Training data access
│   └── EnrollmentRepository.java       # Enrollment data access
├── service/
│   └── TrainingService.java            # Business logic layer
└── TrainingBookingServiceApplication.java  # Main application
```

---

## 🔒 Business Rules Enforced

1. ✅ **User can book a training only once** (unique constraint + validation)
2. ✅ **Training capacity cannot be exceeded** (atomic check and increment)
3. ✅ **Enrollment records are consistent** (transaction management)
4. ✅ **All database operations are atomic** (rollback on failure)

---

## 🚧 Future Enhancements

- **Authentication & Authorization**: JWT-based auth with role-based access (Admin, User)
- **Training Cancellation**: Allow users to cancel enrollments
- **Waiting List**: Queue users when training is full
- **Email Notifications**: Notify users on successful booking/cancellation
- **Pagination**: Add pagination for user trainings list
- **MapStruct**: Use MapStruct for DTO-Entity mapping
- **Caching**: Redis caching for frequently accessed data
- **Rate Limiting**: Prevent API abuse
- **Docker**: Containerize application
- **CI/CD**: GitHub Actions for automated testing and deployment

---

## 🛠️ Technologies Used

- **Spring Boot 3.2.x** - Application framework
- **Spring Data JPA** - Data access layer
- **H2 Database** - In-memory database
- **Lombok** - Reduce boilerplate code
- **Maven** - Build tool
- **SLF4J** - Logging

---

## 👤 Author

**Anish Jaiswal**  
GitHub: [@AnishJaiswal4444](https://github.com/AnishJaiswal4444)

---

## 📄 License

This project is created for demonstration purposes.

---

## 🤝 Contributing

This is a demonstration project. Feel free to fork and experiment!

---

## 📞 Contact

For questions or feedback, please open an issue on GitHub.