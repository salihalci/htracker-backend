# Habit Tracker Backend

A RESTful API service for tracking daily habits and their completions. Built with Spring Boot and SQLite.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Data Model](#data-model)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Building and Running](#building-and-running)
- [Swagger Documentation](#swagger-documentation)
- [Database Schema](#database-schema)

---

## Architecture Overview

The Habit Tracker Backend follows a **layered architecture** pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌─────────────────────────────────────────────────────┐     │
│  │              HabitController (REST API)            │     │
│  └─────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│  ┌─────────────────────────────────────────────────────┐     │
│  │              HabitService (Business Logic)          │     │
│  │  - CRUD operations                                   │     │
│  │  - Streak calculation                               │     │
│  │  - Completion tracking                             │     │
│  └─────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│  ┌──────────────────┐  ┌──────────────────────────────────┐ │
│  │  HabitRepository │  │ HabitCompletionRepository        │ │
│  └──────────────────┘  └──────────────────────────────────┘ │
│                              │                               │
│                              ▼                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              JPA/Hibernate + SQLite                  │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### Design Patterns Used

| Pattern | Implementation |
|---------|----------------|
| **Repository** | Spring Data JPA repositories for data access |
| **Service Layer** | Business logic encapsulated in `HabitService` |
| **DTO** | `HabitRequest` / `HabitResponse` for API data transfer |
| **Transaction** | `@Transactional` annotations for data consistency |

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| **Framework** | Spring Boot 4.0.6 |
| **Language** | Java 25 |
| **Database** | SQLite |
| **ORM** | Hibernate (JPA) |
| **Validation** | Jakarta Validation |
| **API Documentation** | SpringDoc OpenAPI (Swagger) |
| **Build Tool** | Maven |

---

## Project Structure

```
htracker-backend/
├── ht-srvc/
│   ├── src/main/java/com/htbackend/srvc/
│   │   ├── SrvcApplication.java          # Main application entry point
│   │   ├── controller/
│   │   │   └── HabitController.java       # REST API endpoints
│   │   ├── service/
│   │   │   └── HabitService.java          # Business logic
│   │   ├── entity/
│   │   │   ├── Habit.java                  # Habit entity
│   │   │   ├── HabitCompletion.java        # Completion record entity
│   │   │   └── HabitFrequency.java         # Frequency enum
│   │   ├── dto/
│   │   │   ├── HabitRequest.java           # Request DTO
│   │   │   └── HabitResponse.java          # Response DTO
│   │   ├── repository/
│   │   │   ├── HabitRepository.java        # Habit data access
│   │   │   └── HabitCompletionRepository.java  # Completion data access
│   │   ├── config/
│   │   │   └── OpenApiConfig.java          # Swagger configuration
│   │   └── exception/                      # (extensible)
│   ├── src/test/java/com/htbackend/srvc/
│   │   ├── SrvcApplicationTests.java       # Spring Boot context test
│   │   ├── controller/
│   │   │   └── HabitControllerTest.java    # Controller unit tests
│   │   └── service/
│   │       └── HabitServiceTest.java       # Service unit tests
│   ├── src/main/resources/
│   │   └── application.properties          # Application configuration
│   ├── pom.xml                             # Maven dependencies
│   └── habits.db                           # SQLite database
├── commands.txt                            # Quick reference commands
└── README.md                               # This documentation
```

---

## Data Model

### Entity Relationship Diagram

```
┌─────────────────────┐       ┌──────────────────────┐
│       habits        │       │  habit_completions   │
├─────────────────────┤       ├──────────────────────┤
│ id (PK)             │◄──────│ habit_id (FK)        │
│ name                │       │ id (PK)              │
│ description         │       │ completed_date       │
│ frequency           │       │ completed_at         │
│ reminder_time       │       └──────────────────────┘
│ created_at          │
│ updated_at          │
└─────────────────────┘
```

### Entities

#### Habit
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key, auto-generated |
| `name` | String | Habit name (required) |
| `description` | String | Habit description (optional) |
| `frequency` | HabitFrequency | DAILY, WEEKLY, or MONTHLY |
| `reminderTime` | String | Reminder time in HH:mm format |
| `createdAt` | LocalDateTime | Creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |
| `currentStreak` | Integer | Current consecutive completion streak |
| `longestStreak` | Integer | Longest streak ever achieved |

#### HabitCompletion
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key, auto-generated |
| `habit` | Habit | Foreign key to Habit entity |
| `completedDate` | LocalDate | Date when habit was completed |
| `completedAt` | LocalDateTime | Timestamp of completion |

#### HabitFrequency (Enum)
| Value | Description |
|-------|-------------|
| `DAILY` | Habit should be completed every day |
| `WEEKLY` | Habit should be completed once per week |
| `MONTHLY` | Habit should be completed once per month |

---

## API Endpoints

### Base URL
```
http://localhost:8080/api/habits
```

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/habits` | Get all habits with completion data |
| `GET` | `/api/habits/{id}` | Get a specific habit by ID |
| `POST` | `/api/habits` | Create a new habit |
| `PUT` | `/api/habits/{id}` | Update an existing habit |
| `DELETE` | `/api/habits/{id}` | Delete a habit |
| `POST` | `/api/habits/{id}/complete` | Mark habit as complete for a date |
| `DELETE` | `/api/habits/{id}/complete` | Remove completion for a date |

### Request/Response Examples

#### Create Habit
```bash
curl -X POST http://localhost:8080/api/habits \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Morning Exercise",
    "description": "30 minutes of cardio",
    "frequency": "DAILY",
    "reminderTime": "07:00"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Morning Exercise",
  "description": "30 minutes of cardio",
  "frequency": "DAILY",
  "reminderTime": "07:00",
  "createdAt": "2026-04-26T10:00:00",
  "updatedAt": null,
  "currentStreak": 0,
  "longestStreak": 0,
  "completionDates": []
}
```

#### Mark Habit Complete
```bash
curl -X POST http://localhost:8080/api/habits/1/complete \
  -H "Content-Type: application/json" \
  -d '{"date": "2026-04-26"}'
```

**Response:**
```json
{
  "success": true,
  "habitId": 1,
  "date": "2026-04-26",
  "message": "Habit marked as complete"
}
```

#### Get All Habits
```bash
curl -X GET http://localhost:8080/api/habits
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Morning Exercise",
    "description": "30 minutes of cardio",
    "frequency": "DAILY",
    "reminderTime": "07:00",
    "createdAt": "2026-04-26T10:00:00",
    "updatedAt": null,
    "currentStreak": 2,
    "longestStreak": 5,
    "completionDates": [
      "2026-04-25T00:00:00",
      "2026-04-26T00:00:00"
    ]
  }
]
```

---

## Configuration

### Application Properties

```properties
# Application Name
spring.application.name=srvc

# SQLite Database Configuration
spring.datasource.url=jdbc:sqlite:habits.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.datasource.username=
spring.datasource.password=

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.community.dialect.SQLiteDialect

# Disable datasource initialization
spring.sql.init.mode=never
```

### Key Configuration Notes

- **Database**: SQLite stores data in `habits.db` file
- **DDL Auto**: `update` - automatically creates/updates tables
- **Dialect**: Custom SQLite dialect for Hibernate

---

## Building and Running

### Prerequisites

- Java 25+
- Maven

### Quick Start

For quick reference, see [`commands.txt`](commands.txt) for common commands.

### Build

```bash
cd ht-srvc
./mvnw clean compile
```

### Run

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`

### Package

```bash
./mvnw package -DskipTests
java -jar target/srvc-0.0.1-SNAPSHOT.jar
```

### Run Tests

```bash
./mvnw test
```

---

## Testing

### Unit Tests

The project includes comprehensive unit tests with **29 test cases** covering:

#### `HabitServiceTest` (15 tests)
- ✅ CRUD operations (create, read, update, delete)
- ✅ Habit completion operations (mark/unmark complete)
- ✅ Streak calculation scenarios
- ✅ Exception handling

#### `HabitControllerTest` (13 tests)
- ✅ All REST endpoints (GET, POST, PUT, DELETE)
- ✅ Success scenarios (200, 201, 204)
- ✅ Error scenarios (400, 404)
- ✅ Request/response validation

#### `SrvcApplicationTests` (1 test)
- ✅ Spring Boot context loads successfully

### Test Coverage Summary

| Test Class | Tests | Description |
|------------|-------|-------------|
| `HabitServiceTest` | 15 | Business logic testing |
| `HabitControllerTest` | 13 | REST API endpoint testing |
| `SrvcApplicationTests` | 1 | Integration test |
| **Total** | **29** | **100% pass rate** |

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=HabitServiceTest

# Run with coverage report
./mvnw test jacoco:report
```

---

## Swagger Documentation

Once the application is running:

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui/index.html |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs |
| **OpenAPI YAML** | http://localhost:8080/v3/api-docs.yaml |

---

## Database Schema

### Table: habits

```sql
CREATE TABLE habits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    frequency VARCHAR(255) NOT NULL CHECK (frequency IN ('DAILY','WEEKLY','MONTHLY')),
    reminder_time VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Table: habit_completions

```sql
CREATE TABLE habit_completions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    habit_id BIGINT NOT NULL,
    completed_date DATE NOT NULL,
    completed_at TIMESTAMP,
    FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
);
```

### Indexes

```sql
CREATE INDEX idx_completions_habit_id ON habit_completions(habit_id);
CREATE INDEX idx_completions_date ON habit_completions(completed_date);
```

---

## Streak Calculation Logic

The application calculates two types of streaks:

1. **Current Streak**: Consecutive days completed up to today or yesterday
2. **Longest Streak**: Maximum consecutive days ever achieved

### Algorithm

```
1. Fetch all completions for a habit, ordered by date descending
2. For each completion:
   - If it's the first: start tempStreak = 1
   - If consecutive (1 day gap): increment tempStreak
   - If gap > 1: update longestStreak, reset tempStreak
3. Update longestStreak with final tempStreak value
4. Check if current streak is active (completed today or yesterday)
5. Calculate current streak by iterating backwards from most recent
```

---

## Future Enhancements

- [ ] User authentication and authorization
- [ ] Habit categories/tags
- [ ] Statistics dashboard endpoint
- [ ] Reminder notifications
- [ ] Export data to CSV/JSON
- [ ] Multi-user support
- [ ] Habit templates
- [ ] Progress analytics

---

## License

MIT License