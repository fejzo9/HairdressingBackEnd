# Hair Salon Reservation App - Backend

Welcome to the backend of the **Hair Salon Reservation App**! This project is a RESTful API built with Java Spring Boot, designed to manage user authentication, salon appointments, and other backend functionalities.

## Features

- User registration and login with secure password handling
- RESTful APIs for managing users and appointments
- PostgreSQL database integration
- JWT-based authentication (optional future feature)
- Configurable security policies with Spring Security

---

## Table of Contents

- [Getting Started](#getting-started)
- [Technologies Used](#technologies-used)
- [Setup](#setup)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## Getting Started

Follow these instructions to set up the backend locally on your machine.

### Prerequisites

- [Java 17 or newer](https://www.oracle.com/java/technologies/javase-downloads.html)
- [PostgreSQL](https://www.postgresql.org/download/)
- [Gradle](https://gradle.org/)
- [Git](https://git-scm.com/)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/fejzo9/hair-salon-backend.git
   cd hair-salon-backend
   
2. Set up the database:

Create a PostgreSQL database named hair_salon.
Configure database credentials in application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/hair_salon
spring.datasource.username=fejzo9
spring.datasource.password=pwssswww

3. Run the application:
./gradlew bootRun

The backend will be available at http://localhost:8080

### Technologies Used

- Spring Boot - Backend framework
- PostgreSQL - Database
- Spring Security - Authentication and authorization
- JUnit & Mockito - Testing

### Project Structure
hair-salon-backend/
├── src/main/java/com/hairbooking
│   ├── controller      # REST Controllers
│   ├── service         # Business Logic
│   ├── repository      # Database Access
│   ├── model           # Entities and DTOs
│   └── config          # Security and Application Config
├── src/test/java/com/hairbooking
│   ├── service         # Unit and Integration Tests
└── build.gradle        # Gradle Build Configuration

