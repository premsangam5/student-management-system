# Student Management System

A REST API backend for managing student records, built with Java, Spring Boot, and MySQL.

## Tech Stack

- Java 17
- Spring Boot 3.3
- Spring JDBC (`JdbcTemplate`)
- MySQL
- Maven
- Bean Validation (Jakarta Validation)

## Architecture

```
Controller  -->  Service  -->  Repository  -->  MySQL
(HTTP layer)   (business logic)  (JDBC/SQL)
```

- **Controller** (`StudentController`) — handles HTTP requests/responses and status codes.
- **Service** (`StudentServiceImpl`) — business rules (e.g. duplicate email checks, existence checks).
- **Repository** (`StudentRepositoryImpl`) — raw SQL via `JdbcTemplate`, using parameterized queries.
- **GlobalExceptionHandler** — centralizes error handling so every endpoint returns consistent JSON errors and correct HTTP status codes.

## Setup

### 1. Prerequisites
- JDK 17+
- Maven 3.8+
- MySQL 8+ running locally (or update the connection URL)

### 2. Configure the database
Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/student_management_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

The `students` table is created automatically on startup from `schema.sql` — no manual DB setup needed beyond having MySQL running.

### 3. Run the app

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

### 4. Run tests

```bash
mvn test
```

## API Endpoints

| Method | Endpoint              | Description              | Success Status |
|--------|------------------------|---------------------------|-----------------|
| GET    | `/api/students`        | Get all students          | 200 OK          |
| GET    | `/api/students/{id}`   | Get a student by ID       | 200 OK / 404    |
| POST   | `/api/students`        | Create a new student      | 201 Created     |
| PUT    | `/api/students/{id}`   | Update an existing student| 200 OK / 404    |
| DELETE | `/api/students/{id}`   | Delete a student          | 204 No Content / 404 |

### Sample request body (POST / PUT)

```json
{
  "firstName": "Aditi",
  "lastName": "Sharma",
  "email": "aditi.sharma@example.com",
  "age": 21,
  "department": "Computer Science"
}
```

### Sample validation error response (400)

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-08-02T10:15:30",
  "validationErrors": {
    "email": "Email must be a valid email address",
    "firstName": "First name is required"
  }
}
```

### Sample not-found response (404)

```json
{
  "status": 404,
  "message": "Student not found with id: 99",
  "timestamp": "2026-08-02T10:16:02"
}
```

## Security practices applied

- **Input validation** via Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Min`/`@Max`) on the `Student` model, enforced with `@Valid` in the controller.
- **Parameterized SQL queries** (`PreparedStatement` via `JdbcTemplate`) throughout the repository layer to prevent SQL injection — no string concatenation of user input into SQL.
- **Centralized exception handling** so raw stack traces / internal error details are never returned to the client; only structured, appropriate HTTP status codes and messages.
- **Unique email constraint** enforced at both the application layer (service check) and database layer (`UNIQUE` column).

## Testing with Postman

1. Import the endpoints above into a Postman collection (or create requests manually).
2. Set `Content-Type: application/json` on POST/PUT requests.
3. Test the full CRUD flow:
   - `POST /api/students` → create a student, confirm `201` and the returned `id`.
   - `GET /api/students` → confirm the new student appears in the list.
   - `GET /api/students/{id}` → confirm `200` with correct data; try an invalid id to confirm `404`.
   - `PUT /api/students/{id}` → update a field, confirm `200` with updated data.
   - `DELETE /api/students/{id}` → confirm `204`, then `GET` the same id to confirm `404`.
   - Submit an invalid body (e.g. missing `email`) to a POST request to confirm `400` with validation error details.

## Project Structure

```
student-management-system/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/example/studentmanagement/
    │   │   ├── StudentManagementApplication.java
    │   │   ├── controller/StudentController.java
    │   │   ├── service/StudentService.java
    │   │   ├── service/StudentServiceImpl.java
    │   │   ├── repository/StudentRepository.java
    │   │   ├── repository/StudentRepositoryImpl.java
    │   │   ├── model/Student.java
    │   │   ├── dto/ErrorResponse.java
    │   │   └── exception/
    │   │       ├── ResourceNotFoundException.java
    │   │       └── GlobalExceptionHandler.java
    │   └── resources/
    │       ├── application.properties
    │       └── schema.sql
    └── test/
        └── java/com/example/studentmanagement/StudentManagementApplicationTests.java
```
