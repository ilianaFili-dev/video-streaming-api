# Video Streaming API

A REST API for a video streaming service, built with Spring Boot 3 and PostgreSQL. It handles user registration with subscription management and payment processing.

---

## Tech Stack

- Java 17
- Spring Boot 3.5
- Spring Data JPA / Hibernate
- Spring Security 6
- PostgreSQL
- Apache Commons Validator (Luhn algorithm)
- JUnit 5 / Mockito

---

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

---

## Database Setup

Create the database before starting the application:

```sql
CREATE DATABASE video_streaming;
```

Then update the credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/video_streaming
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Hibernate will create the tables automatically on first run (`ddl-auto=update`).

---

## Running the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## API Endpoints

### Register a User

```
POST /api/users
```

**Request body:**
```json
{
  "username": "iliana_dev",
  "password": "SecurePass123",
  "email": "iliana@example.com",
  "dateOfBirth": "1990-05-15",
  "creditCardNumber": "4532015112830366"
}
```

`creditCardNumber` is optional. If provided, it must pass the Luhn algorithm checksum.

**Responses:**
| Status | Reason |
|--------|--------|
| 201 Created | User registered successfully |
| 400 Bad Request | Validation failure (format, Luhn check) |
| 403 Forbidden | User is under 18 |
| 409 Conflict | Username, email, or credit card already registered |

**201 response body:**
```json
{
  "id": 1,
  "username": "iliana_dev",
  "email": "iliana@example.com",
  "subscriptionStatus": "TRIAL",
  "trialEndDate": "2026-04-16T10:00:00",
  "hasCreditCard": true,
  "createdAt": "2026-04-01T10:00:00"
}
```

---

### List Users

```
GET /api/users
GET /api/users?creditCard=Yes
GET /api/users?creditCard=No
```

Returns all users, optionally filtered by whether they have a credit card registered.

**200 response body:**
```json
{
  "users": [
    {
      "id": 1,
      "username": "iliana_dev",
      "email": "iliana@example.com",
      "subscriptionStatus": "TRIAL",
      "trialEndDate": "2026-04-16T10:00:00",
      "hasCreditCard": true,
      "createdAt": "2026-04-01T10:00:00"
    }
  ],
  "total": 1
}
```

---

### Process a Payment

```
POST /api/payments
```

**Request body:**
```json
{
  "creditCardNumber": "4532015112830366",
  "amount": "99.99"
}
```

Amount must be between 1.00 and 999.99. The card must be registered to an existing user.

**Responses:**
| Status | Reason |
|--------|--------|
| 201 Created | Payment processed, subscription activated |
| 400 Bad Request | Validation failure (card format, amount out of range) |
| 404 Not Found | Credit card not registered to any user |

**201 response body:**
```json
{
  "id": 1,
  "username": "iliana_dev",
  "amount": 99.99,
  "paymentStatus": "SUCCESS",
  "creditCardLastFour": "0366",
  "createdAt": "2026-04-01T10:00:00"
}
```

---

## Validation Rules

**Username:** alphanumeric, 3–50 characters, no spaces  
**Password:** minimum 8 characters, at least 1 uppercase letter and 1 digit  
**Email:** standard email format  
**Date of birth:** `yyyy-MM-dd`, user must be 18 or older  
**Credit card:** exactly 16 digits, must pass Luhn checksum

---

## Design Decisions

**Credit card hashing — HMAC-SHA256 instead of BCrypt**

BCrypt is intentionally non-deterministic: the same input produces a different hash on every call. This makes it well-suited for passwords (verified with `matches()`), but it breaks any use case that requires database lookup by hash value.

For credit cards, two things need to work: detecting duplicates at registration, and finding the card owner at payment time. Both require looking up a record by hash. BCrypt cannot support this. HMAC-SHA256 with a fixed secret key is deterministic — the same card number always produces the same hash — which makes these lookups reliable. The secret is stored in `application.properties` and should be rotated and externalised in a production environment.

The full card number is never stored. Only the HMAC hash (for lookups) and the last four digits (for display) are persisted.

The HMAC secret for credit card hashing is configured in application.properties:
app.credit-card.hmac-secret=change-me-in-production-use-32-chars

**User lookup at payment time**

The payment request only requires `creditCardNumber` and `amount`. There is no `username` field. The service computes the card's HMAC hash and looks up the owning user directly — if the card is not registered, the payment is rejected with 404. This avoids the user having to send a username alongside their card number, and means ownership is verified implicitly through the hash lookup rather than as a separate step.

**Validation in two layers**

Format validation (@NotBlank, @Pattern, @Email, @DecimalMin, etc.) lives in the DTOs and is enforced by @Valid at the controller level. Business rule validation (age check, Luhn algorithm, duplicate detection) lives in the service layer. This separation means invalid formats never reach the service, and the service stays focused on logic rather than parsing.

**`ddl-auto=update` instead of `create-drop`**

`create-drop` drops and recreates all tables on each application shutdown, which causes problems if the process is killed rather than stopped cleanly. `update` creates tables if they don't exist and applies schema changes incrementally, which is much safer during development.

---

## Running the Tests

```bash
mvn test
```

The test suite covers both layers:

- **Service tests** (`UserServiceTest`, `PaymentServiceTest`) — pure unit tests with Mockito mocks, no Spring context
- **Controller tests** (`UserControllerTest`, `PaymentControllerTest`) — `@WebMvcTest` with MockMvc, testing HTTP behaviour, validation, and exception handling

21 tests in total.
All tests pass with 0 failures. Service tests run without a database connection;
controller tests use @WebMvcTest to test HTTP behaviour in isolation.
