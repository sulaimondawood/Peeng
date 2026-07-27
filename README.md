# Peeng

**A comprehensive Spring Boot-based incident management and monitoring platform with multi-tenant support, real-time notifications, and advanced security features.**

Peeng is an enterprise-grade incident management system designed to help teams monitor, track, and manage incidents across multiple organizations. Built on Spring Boot 4.0.6 with Java 21, it provides a robust backend infrastructure for incident response workflows, user management, subscriptions, and real-time messaging.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Usage Examples](#usage-examples)
- [Database Setup](#database-setup)
- [Message Queue](#message-queue)
- [Email Configuration](#email-configuration)
- [Security](#security)
- [Contributing](#contributing)
- [License](#license)

## Features

### Core Functionality

- **Incident Management**: Create, update, monitor, and resolve incidents with full lifecycle tracking
- **Multi-Tenant Support**: Complete tenant isolation with organization-level data segregation
- **User Identity & Management**: Comprehensive user authentication, registration, and profile management
- **Role-Based Access Control**: Fine-grained permission management via tenant and membership modules
- **Subscription Management**: Flexible subscription plans and tier management for organizations
- **Real-Time Monitoring**: System monitoring and health checks with scheduled tasks
- **Notifications**: Real-time event-driven notifications with email support
- **Messaging System**: Asynchronous message queue processing using RabbitMQ
- **Dashboard Analytics**: Aggregate incident data and metrics for organizational dashboards
- **JWT Security**: Secure API endpoints with JWT token-based authentication

### Technical Features

- Spring Security integration with JWT authentication
- Spring Data JPA for ORM and database operations
- PostgreSQL persistence with Hibernate
- RabbitMQ for asynchronous message processing
- Email notification system with SMTP integration
- Thymeleaf templating for email templates
- Comprehensive validation framework
- Scheduled task support for periodic operations

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.6
- **Database**: PostgreSQL 14
- **Message Broker**: RabbitMQ 4.1.4
- **Build Tool**: Maven 3
- **Key Libraries**:
  - Spring Data JPA (ORM)
  - Spring Security (Authentication & Authorization)
  - Spring Web MVC (REST API)
  - Auth0 Java-JWT 4.5.2 (JWT handling)
  - Lombok (Boilerplate reduction)
  - Spring Validation (Input validation)
  - Thymeleaf (Template engine)

## Project Structure

```
src/main/java/com/dawood/peeng/
├── PeengApplication.java          # Spring Boot entry point
├── common/                         # Shared utilities and exceptions
│   ├── ResponseBuilder.java       # API response standardization
│   ├── dto/                       # Common DTOs
│   ├── enums/                     # Shared enumerations
│   ├── exceptions/                # Custom exception classes
│   └── models/                    # Base entity models
├── configs/                        # Application configuration classes
├── security/                       # JWT and authentication config
├── identity/                       # User & authentication module
│   ├── controller/                # Authentication endpoints
│   ├── service/                   # User management logic
│   ├── repository/                # User data access
│   ├── models/                    # User entities
│   ├── dtos/                      # Authentication DTOs
│   ├── exceptions/                # Identity-specific exceptions
│   ├── mapper/                    # Entity-DTO mapping
│   └── event/                     # Authentication events
├── tenant/                         # Multi-tenant management
│   ├── service/                   # Tenant operations
│   ├── repository/                # Tenant data access
│   └── models/                    # Tenant entities
├── membership/                     # Organization membership
│   ├── service/                   # Membership logic
│   ├── repository/                # Membership data access
│   └── models/                    # Membership entities
├── subscriptions/                  # Subscription management
│   ├── service/                   # Subscription handling
│   ├── repository/                # Subscription data access
│   └── models/                    # Subscription entities
├── incident/                       # Incident management
│   ├── controller/                # Incident API endpoints
│   ├── service/                   # Incident business logic
│   ├── repository/                # Incident data access
│   ├── models/                    # Incident entities
│   └── dtos/                      # Incident DTOs
├── monitor/                        # System monitoring
│   ├── service/                   # Monitoring logic
│   └── models/                    # Monitoring entities
├── dashboard/                      # Analytics & reporting
│   ├── controller/                # Dashboard endpoints
│   ├── service/                   # Dashboard metrics
│   └── dtos/                      # Dashboard DTOs
├── notification/                   # Notification system
│   ├── service/                   # Notification dispatch
│   ├── repository/                # Notification storage
│   └── models/                    # Notification entities
├── messaging/                      # RabbitMQ integration
│   ├── publisher/                 # Message publishing
│   └── consumer/                  # Message consumption
└── utils/                          # Utility functions

src/main/resources/
├── application.yml                # Application configuration
└── templates/                     # Thymeleaf email templates
```

### Data Flow

The application follows a layered architecture:
1. **API Layer** (Controllers): Handle HTTP requests and REST endpoints
2. **Service Layer**: Contain business logic, validation, and orchestration
3. **Data Access Layer** (Repositories): Manage database operations via Spring Data JPA
4. **Event/Messaging Layer**: Asynchronous event processing via RabbitMQ
5. **Security Layer**: JWT authentication and authorization filters

## Prerequisites

Before running Peeng, ensure you have installed:

- **Java 21 or higher** - [Download Java](https://www.oracle.com/java/technologies/downloads/)
- **Docker & Docker Compose** - [Install Docker](https://docs.docker.com/get-docker/)
- **Maven 3.6+** - [Download Maven](https://maven.apache.org/download.cgi)
- **PostgreSQL 14** (optional if using Docker Compose)
- **RabbitMQ 4.1.4** (optional if using Docker Compose)

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/sulaimondawood/Peeng.git
cd Peeng
```

### 2. Start Infrastructure Services

Use Docker Compose to start PostgreSQL and RabbitMQ:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on `localhost:5432`
- RabbitMQ on `localhost:5672` (AMQP) and `localhost:15672` (Management UI)

**Verify services are running:**
```bash
docker-compose ps
```

### 3. Build the Application

```bash
# Using Maven Wrapper (recommended)
./mvnw clean package

# Or using system Maven
mvn clean package
```

## Configuration

### Environment Variables

Create a `.env` file in the project root directory with the following variables:

```properties
# JWT Configuration
JWT_SECRET=your-secret-key-minimum-32-characters-long

# SMTP Email Configuration
SMTP_HOST=smtp.mailtrap.io
SMTP_USERNAME=your-mailtrap-username
SMTP_PASSWORD=your-mailtrap-password
MAIL_USER_DEV=dev@peeng.local

# Client Configuration
CLIENT_URL=http://localhost:3000
```

### Database Configuration

Edit `src/main/resources/application.yml` to configure database connections:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/peeng_db
    username: admin
    password: admin
  jpa:
    hibernate:
      ddl-auto: update
```

**DDL Options:**
- `create-drop`: Drop tables on shutdown and recreate on startup (dev only)
- `update`: Update schema based on entity definitions (recommended for dev)
- `validate`: Validate schema without modifications (production)
- `none`: No automatic schema changes

### RabbitMQ Configuration

The application is pre-configured for RabbitMQ. Edit `application.yml` to customize:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: password
    listener:
      simple:
        retry:
          enabled: true
          max-retries: 3
          initial-interval: 2000ms
          multiplier: 2
```

## Running the Application

### Option 1: Maven Wrapper (Recommended)

```bash
./mvnw spring-boot:run
```

### Option 2: Java Command

```bash
java -jar target/peeng-0.0.1-SNAPSHOT.jar
```

### Option 3: IDE

1. Open the project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Run `PeengApplication.main()` as a Java application

**Default Server Configuration:**
- API Base URL: `http://localhost:8080/api/v1`
- Application Name: `peeng`

## API Documentation

The application provides RESTful API endpoints organized by module:

### Base URL
```
http://localhost:8080/api/v1
```

### Major Endpoints

#### Identity Module
- `POST /auth/register` - Register new user
- `POST /auth/login` - Authenticate user
- `POST /auth/refresh` - Refresh JWT token
- `GET /users/profile` - Get current user profile
- `PUT /users/profile` - Update user profile

#### Tenant Module
- `POST /tenants` - Create organization
- `GET /tenants/{id}` - Get tenant details
- `PUT /tenants/{id}` - Update tenant
- `DELETE /tenants/{id}` - Delete tenant

#### Incident Module
- `POST /incidents` - Create incident
- `GET /incidents` - List incidents
- `GET /incidents/{id}` - Get incident details
- `PUT /incidents/{id}` - Update incident
- `DELETE /incidents/{id}` - Delete incident

#### Dashboard Module
- `GET /dashboard/metrics` - Get organization metrics
- `GET /dashboard/incidents/summary` - Incident summary

#### Subscription Module
- `GET /subscriptions/{tenantId}` - Get tenant subscription
- `PUT /subscriptions/{tenantId}` - Update subscription

## Usage Examples

### 1. User Registration

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com"
  }
}
```

### 2. User Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "123e4567-e89b-12d3-a456-426614174000"
  }
}
```

### 3. Create Incident

```bash
curl -X POST http://localhost:8080/api/v1/incidents \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "title": "Database Connection Failure",
    "description": "Production database is unreachable",
    "severity": "CRITICAL",
    "status": "OPEN",
    "assignee": "team-lead@example.com"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Incident created successfully",
  "data": {
    "incidentId": "inc_abc123",
    "title": "Database Connection Failure",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

### 4. List Incidents

```bash
curl -X GET "http://localhost:8080/api/v1/incidents?status=OPEN&page=0&size=10" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 5. Update Incident

```bash
curl -X PUT http://localhost:8080/api/v1/incidents/inc_abc123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "status": "RESOLVED",
    "resolution": "Database server restarted successfully"
  }'
```

## Database Setup

### Initial Database Creation

The application automatically creates required tables on first run due to `ddl-auto: update` setting.

### Manual Database Setup

If needed, connect to PostgreSQL and create the database:

```bash
# Access PostgreSQL
docker exec -it peeng_db psql -U admin

# Create database
CREATE DATABASE peeng_db;
\c peeng_db

# Exit
\q
```

### Database Schema

Core tables created automatically:
- `users` - User accounts and authentication
- `tenants` - Organization data
- `memberships` - Organization membership records
- `incidents` - Incident tracking
- `subscriptions` - Subscription data
- `notifications` - Notification records
- `monitoring_data` - System monitoring metrics

## Message Queue

### RabbitMQ Management UI

Access RabbitMQ management console:
```
http://localhost:15672
Default credentials: admin / password
```

### Message Types

The application publishes and consumes:
- User registration events
- Incident creation/update events
- Notification dispatch events
- Email send requests
- Monitoring alerts

### Monitoring Queues

```bash
# View queue status
curl http://admin:password@localhost:15672/api/queues

# List exchanges
curl http://admin:password@localhost:15672/api/exchanges
```

## Email Configuration

### SMTP Setup

The application sends emails via SMTP. Configure in `.env`:

```properties
SMTP_HOST=smtp.gmail.com
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

### Email Features

- User registration confirmation
- Password reset emails
- Incident notifications
- Subscription alerts

### Using Mailtrap (Recommended for Development)

1. Sign up at [Mailtrap.io](https://mailtrap.io)
2. Get SMTP credentials from dashboard
3. Update `.env` with Mailtrap credentials
4. All emails will be captured in Mailtrap inbox (no actual sending)

## Security

### Authentication

The application uses JWT (JSON Web Token) for stateless authentication:

1. User logs in with credentials
2. Server returns JWT token
3. Client includes token in `Authorization: Bearer <token>` header
4. Server validates token on each request

### JWT Configuration

Set `JWT_SECRET` in `.env` to a strong, random value (minimum 32 characters):

```bash
# Generate a secure secret
openssl rand -hex 32
```

### Authorization

Role-based access control is implemented through:
- Tenant membership levels
- Permission annotations on endpoints
- Membership service authorization checks

### Security Best Practices

1. **Never commit `.env` file** - Add to `.gitignore`
2. **Use HTTPS in production** - Configure SSL/TLS
3. **Rotate JWT secrets regularly** - Update `JWT_SECRET` periodically
4. **Validate all inputs** - Validation annotations prevent injection attacks
5. **Database credentials** - Use environment variables, never hardcode
6. **CORS configuration** - Configure allowed origins in `configs/`

## Troubleshooting

### Common Issues

**Port Already in Use**
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

**Database Connection Error**
```
Verify Docker containers are running:
docker-compose ps

Restart services:
docker-compose restart
```

**RabbitMQ Connection Failed**
```
Check RabbitMQ status:
docker-compose logs peeng_queue

Verify credentials in application.yml
```

**JWT Token Expired**
```
Call the refresh endpoint:
POST /auth/refresh with expired token

Or log in again to get new token
```

## Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Spring Boot conventions
- Use Lombok for reducing boilerplate
- Write meaningful commit messages
- Add tests for new features

## License

This project is currently unlicensed. Please contact the repository owner for licensing information.

---

**Built with ❤️ by [sulaimondawood](https://github.com/sulaimondawood)**

For issues, questions, or suggestions, please open an [issue on GitHub](https://github.com/sulaimondawood/Peeng/issues).
