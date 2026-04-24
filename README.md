# 🧠 PsyCare — Psychology Practice Management API

REST API developed with **Spring Boot 3** for managing a psychology practice. Handles patients, appointments, clinical records, therapy sessions, payments, tutors and medical insurances, with stateless JWT-based authentication.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Error Handling](#error-handling)
- [Configuration & Environment Variables](#configuration--environment-variables)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [API Documentation (Swagger)](#api-documentation-swagger)

---

## Overview

PsyCare is a backend REST API designed to assist a psychologist in managing their practice. It covers:

- **Patient management**: registration, update, discharge and reactivation, with minor patient support (requires a tutor).
- **Appointment scheduling**: creation, update, cancellation and attendance marking, with intelligent slot availability via the Agenda module.
- **Clinical records**: one record per patient, updated over time, linked to therapy sessions.
- **Therapy sessions**: individual session notes linked to a patient's clinical record.
- **Payments**: registration and tracking of payments associated with appointments.
- **Tutors**: management of legal guardians/parents for minor patients.
- **Medical insurances**: management of available health insurance providers.
- **Authentication**: single-user JWT authentication with password update support.

---

## Tech Stack

| Layer           | Technology                                                 |
| --------------- | ---------------------------------------------------------- |
| Language        | Java 17                                                    |
| Framework       | Spring Boot 3.5.11                                         |
| Security        | Spring Security 6 + JWT (Auth0 `java-jwt` 4.5.0)           |
| Persistence     | Spring Data JPA / Hibernate                                |
| Database (prod) | MySQL                                                      |
| Database (test) | H2 (in-memory)                                             |
| Validation      | Jakarta Bean Validation                                    |
| API Docs        | SpringDoc OpenAPI 3 / Swagger UI 2.8.5                     |
| Utilities       | Lombok, `dotenv-java` 3.0.0                                |
| Testing         | JUnit 5, Mockito 5, Spring Boot Test, Spring Security Test |
| Build           | Maven (Maven Wrapper included)                             |

---

## Project Structure

```
src/
├── main/java/com/camicompany/PsyCare/
│   ├── PsyCareApplication.java
│   ├── common/                        # BaseEntity
│   ├── config/                        # OpenAPI / Swagger config
│   ├── controller/                    # REST controllers
│   │   ├── AgendaController.java
│   │   ├── AppointmentController.java
│   │   ├── AuthController.java
│   │   ├── ClinicalRecordController.java
│   │   ├── InsuranceController.java
│   │   ├── PatientController.java
│   │   ├── PaymentController.java
│   │   ├── SessionController.java
│   │   └── TutorController.java
│   ├── dto/                           # Request / Response records
│   ├── exception/                     # Custom exceptions + GlobalExceptionHandler
│   ├── mapper/                        # Entity ↔ DTO mappers
│   ├── model/                         # JPA entities and enums
│   ├── repository/                    # Spring Data JPA repositories
│   ├── security/config/               # SecurityConfig, JWT filter
│   └── service/                       # Business logic (interfaces + implementations)
│
├── main/resources/
│   ├── application.yml                # Base config (env var references)
│   ├── application-dev.yml            # Dev profile (MySQL)
│   └── application-test.yml           # Test profile (H2 in-memory)
│
└── test/java/com/camicompany/PsyCare/
    ├── controller/                    # @WebMvcTest unit tests per controller
    ├── service/                       # @ExtendWith(MockitoExtension) unit tests per service
    ├── Integration/                   # @SpringBootTest full integration tests (H2, no security)
    └── SecurityIntegration/           # @SpringBootTest security tests (real JWT filter)
```

---

## Domain Model

### Entities

| Entity           | Description                                                                                                                                                |
| ---------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Patient`        | Main entity. Can be a minor (requires a `Tutor`). Has a `PatientStatus` (ACTIVE / DISCHARGED) and may have an associated `Insurance` and `ClinicalRecord`. |
| `Tutor`          | Legal guardian or parent of a minor patient. Linked with a `TutorRelation` (MOTHER / FATHER / LEGAL_GUARDIAN).                                             |
| `Insurance`      | Medical insurance provider. Has name and CUIT.                                                                                                             |
| `Appointment`    | Scheduled visit. Can store either a registered `Patient` or ad-hoc contact data. Has `AppointmentStatus`, `AppointmentType`, price, and payment tracking.  |
| `Payment`        | Payment record associated with an `Appointment`. Has `PaymentStatus` (PENDING / PAID / CANCELLED) and amount.                                              |
| `ClinicalRecord` | One per patient. Stores reason for consult, diagnosis, observations, and medication. Contains a list of `Session`s.                                        |
| `Session`        | Individual therapy session note. Linked to a `ClinicalRecord`. Stores date and evolution notes.                                                            |
| `User`           | Single application user (the psychologist). Used for authentication.                                                                                       |

### Enums

| Enum                       | Values                                         |
| -------------------------- | ---------------------------------------------- |
| `PatientStatus`            | `ACTIVE`, `DISCHARGED`                         |
| `AppointmentStatus`        | `SCHEDULED`, `CANCELLED`, `ATTENDED`           |
| `AppointmentType`          | `GENERAL`, `FIRST_CONSULTATION`, _(others)_    |
| `AppointmentPaymentStatus` | Tracks overall payment state of an appointment |
| `PaymentStatus`            | `PENDING`, `PAID`, `CANCELLED`                 |
| `TutorRelation`            | `MOTHER`, `FATHER`, `LEGAL_GUARDIAN`           |

---

## API Endpoints

All endpoints (except login and Swagger docs) require a valid JWT in the `Authorization: Bearer <token>` header.

### 🔐 Auth — `/api/v1/auth`

| Method  | Path               | Description                        | Auth required |
| ------- | ------------------ | ---------------------------------- | ------------- |
| `POST`  | `/login`           | Authenticate and receive JWT token | ❌            |
| `PATCH` | `/update-password` | Update the user's password         | ✅            |

### 📅 Agenda — `/api/v1/agenda`

| Method | Path                               | Description                                                                                       |
| ------ | ---------------------------------- | ------------------------------------------------------------------------------------------------- |
| `GET`  | `/available-slots?date=YYYY-MM-DD` | Returns available appointment slots for a given date (excludes weekends and already booked slots) |

### 🧍 Patients — `/api/v1/patients`

| Method  | Path              | Description                                       |
| ------- | ----------------- | ------------------------------------------------- |
| `GET`   | `/`               | List all patients (summary)                       |
| `GET`   | `/{id}`           | Get full patient details                          |
| `POST`  | `/`               | Register a new patient                            |
| `PATCH` | `/{id}`           | Partially update patient data                     |
| `PUT`   | `/{id}/discharge` | Discharge a patient (status → DISCHARGED)         |
| `PUT`   | `/{id}/reactive`  | Reactivate a discharged patient (status → ACTIVE) |

> **Note**: Minor patients (under 18) require a tutor. You can provide either an existing `tutorId` or a full `tutor` object — not both.

### 📆 Appointments — `/api/v1/appointments`

| Method  | Path             | Description                                                       |
| ------- | ---------------- | ----------------------------------------------------------------- |
| `GET`   | `/{id}`          | Get appointment by ID                                             |
| `POST`  | `/`              | Create a new appointment (with registered patient or ad-hoc data) |
| `PATCH` | `/{id}`          | Partially update an appointment                                   |
| `PUT`   | `/{id}/cancel`   | Cancel an appointment                                             |
| `PUT`   | `/{id}/attended` | Mark an appointment as attended                                   |

### 💊 Insurances — `/api/v1/insurances`

| Method  | Path           | Description                   |
| ------- | -------------- | ----------------------------- |
| `GET`   | `/`            | List all insurances           |
| `GET`   | `/{id}`        | Get insurance by ID           |
| `GET`   | `/name/{name}` | Get insurance by name         |
| `POST`  | `/`            | Create a new insurance        |
| `PATCH` | `/{id}`        | Partially update an insurance |

### 💰 Payments — `/api/v1/payments`

| Method  | Path                            | Description                                                 |
| ------- | ------------------------------- | ----------------------------------------------------------- |
| `GET`   | `/`                             | List payments filtered by date range (`?start=...&end=...`) |
| `GET`   | `/{id}`                         | Get payment by ID                                           |
| `GET`   | `/appointments/{appointmentId}` | Get all payments for a specific appointment                 |
| `POST`  | `/appointments/{appointmentId}` | Register a payment for an appointment                       |
| `PATCH` | `/{id}`                         | Update payment details                                      |
| `PUT`   | `/{id}/cancel`                  | Cancel a payment                                            |

### 📁 Clinical Records — `/api/v1`

| Method  | Path                                    | Description                                              |
| ------- | --------------------------------------- | -------------------------------------------------------- |
| `GET`   | `/clinical-records/{id}`                | Get clinical record by ID (includes all sessions)        |
| `POST`  | `/patients/{patientId}/clinical-record` | Create a clinical record for a patient (one per patient) |
| `PATCH` | `/clinical-records/{id}`                | Partially update a clinical record                       |

### 🗒️ Sessions — `/api/v1`

| Method  | Path                                            | Description                            |
| ------- | ----------------------------------------------- | -------------------------------------- |
| `GET`   | `/sessions/{id}`                                | Get session by ID                      |
| `POST`  | `/clinical-records/{clinicalRecordId}/sessions` | Add a new session to a clinical record |
| `PATCH` | `/sessions/{id}`                                | Partially update a session             |

### 👨‍👩‍👦 Tutors — `/api/v1/tutors`

| Method  | Path             | Description                                |
| ------- | ---------------- | ------------------------------------------ |
| `POST`  | `/`              | Create a new tutor                         |
| `PATCH` | `/{id}`          | Partially update tutor information         |
| `PUT`   | `/{id}/relation` | Update the tutor-patient relationship type |

---

## Security

The application uses **stateless JWT authentication** with Spring Security 6.

### Authentication Flow

1. Client sends credentials to `POST /api/v1/auth/login`.
2. Server validates credentials and returns a signed JWT token.
3. Client includes the token in every subsequent request: `Authorization: Bearer <token>`.
4. The `JwtTokenValidator` filter validates the token on each request before it reaches the controller.

### Security Rules

| Scenario                              | Response                                                      |
| ------------------------------------- | ------------------------------------------------------------- |
| No token provided                     | `401 UNAUTHORIZED` — "Access denied: authentication required" |
| Invalid or expired token              | `401 UNAUTHORIZED` — "Invalid or expired token"               |
| Valid token, insufficient permissions | `403 FORBIDDEN`                                               |

### Configuration

- Passwords are encoded with **BCrypt**.
- Sessions are **stateless** (no `HttpSession`).
- CSRF is **disabled** (stateless REST API).
- Token signing uses a private key configured via environment variables.
- Swagger UI and `/api/v1/auth/login` are publicly accessible.

---

## Error Handling

All errors are returned as a standard JSON body via `GlobalExceptionHandler`:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Patient not found with id: 99",
  "timestamp": "2026-04-24T10:30:00"
}
```

### Exception Mapping

| Exception                              | HTTP Status                 |
| -------------------------------------- | --------------------------- |
| `ResourceNotFoundException`            | `404 NOT_FOUND`             |
| `StatusConflictException`              | `409 CONFLICT`              |
| `DuplicateNationalIdException`         | `409 CONFLICT`              |
| `ConflictingTutorInformationException` | `409 CONFLICT`              |
| `MissingTutorException`                | `400 BAD_REQUEST`           |
| `IllegalArgumentException`             | `400 BAD_REQUEST`           |
| `MethodArgumentNotValidException`      | `400 BAD_REQUEST`           |
| `MethodArgumentTypeMismatchException`  | `400 BAD_REQUEST`           |
| `BadCredentialsException`              | `401 UNAUTHORIZED`          |
| `AuthorizationDeniedException`         | `401 UNAUTHORIZED`          |
| `Exception` (generic)                  | `500 INTERNAL_SERVER_ERROR` |

---

## Configuration & Environment Variables

The application uses environment variables (loaded via `dotenv-java` from a `.env` file in the project root).

### Required Variables

| Variable                  | Description                           | Example               |
| ------------------------- | ------------------------------------- | --------------------- |
| `APP_USERNAME`            | Application username for login        | `admin`               |
| `APP_PASSWORD`            | Application password for login        | `mypassword123`       |
| `JWT_PRIVATE_KEY`         | Secret key for signing JWT tokens     | `my-super-secret-key` |
| `JWT_USER_GENERATOR`      | Issuer/generator name embedded in JWT | `PsyCare`             |
| `AGENDA_START_HOUR`       | Working day start hour                | `9`                   |
| `AGENDA_END_HOUR`         | Working day end hour                  | `18`                  |
| `AGENDA_SESSION_DURATION` | Duration of each session in minutes   | `50`                  |

### Dev Profile Additional Variables

| Variable       | Description       | Example                               |
| -------------- | ----------------- | ------------------------------------- |
| `DB_URL`       | MySQL JDBC URL    | `jdbc:mysql://localhost:3306/psycare` |
| `DB_USER_NAME` | Database username | `root`                                |
| `DB_PASSWORD`  | Database password | `password`                            |

### `.env` file example

Create a `.env` file in the project root:

```env
APP_USERNAME=admin
APP_PASSWORD=password123

JWT_PRIVATE_KEY=your-very-long-secret-key-here
JWT_USER_GENERATOR=PsyCare

AGENDA_START_HOUR=9
AGENDA_END_HOUR=18
AGENDA_SESSION_DURATION=50

DB_URL=jdbc:mysql://localhost:3306/psycare
DB_USER_NAME=root
DB_PASSWORD=yourpassword
```

> ⚠️ Never commit the `.env` file to version control. Add it to `.gitignore`.

---

## Running the Application

### Prerequisites

- Java 17+
- MySQL running locally (for dev profile)
- `.env` file configured in the project root

### Start with dev profile (MySQL)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API will be available at: `http://localhost:8080`

### Build the project

```bash
./mvnw clean package
```

### Run the JAR

```bash
java -jar target/PsyCare-0.0.1.jar --spring.profiles.active=dev
```

---

## Running Tests

Tests use the `test` profile, which automatically configures an **H2 in-memory database** — no MySQL required.

### Run all tests

```bash
./mvnw test
```

### Run a specific test class

```bash
./mvnw test -Dtest=PatientSecurityIntegrationTest
```

### Test structure

| Package                | Type        | Description                                                                                                        |
| ---------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------ |
| `controller/`          | Unit        | `@WebMvcTest` — tests controller logic with mocked services, no security filters                                   |
| `service/`             | Unit        | `@ExtendWith(MockitoExtension)` — tests service logic with mocked repositories                                     |
| `Integration/`         | Integration | `@SpringBootTest` — full application context, real H2 DB, security filters **disabled**                            |
| `SecurityIntegration/` | Integration | `@SpringBootTest` — full application context, real H2 DB, security filters **enabled** — verifies 401/403 behavior |

> **Note on SecurityIntegration tests**: These tests obtain a real JWT token via `POST /api/v1/auth/login` and include a `@BeforeEach` that clears the `SecurityContextHolder` to prevent test contamination across test runs.

---

## API Documentation (Swagger)

When the application is running, the interactive Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:

```
http://localhost:8080/v3/api-docs
```

All endpoints are documented with:

- Summary and description
- Request body schema
- All possible response codes and their schemas
- Parameter descriptions and examples
- JWT `Bearer` token requirement indicator

---

## Author

Developed by **CamiCompany** — `com.camicompany.PsyCare`
