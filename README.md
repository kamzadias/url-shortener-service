# Url Shortener Service

A Spring Boot microservice for shortening URLs, built with Java 17, PostgreSQL, Redis, and Docker Compose.

## Features
- Generate and store unique 6-character hashes for long URLs
- Cache URL mappings in Redis for fast lookups
- Persistent storage in PostgreSQL
- Asynchronous batch generation of hash values
- Scheduled cleanup of expired URLs
- Docker Compose setup for local development

## Installation
Clone the repository and build the project:

```bash
git clone https://github.com/yourusername/url-shortener-service.git
cd url-shortener-service
./gradlew clean build
```

## Configuration
The application uses `src/main/resources/application.yaml`. Default settings:

```bash
spring:
  application:
    name: url-shortener-service
  datasource:
    url: jdbc:postgresql://localhost:5432/urlshortener
    username: user
    password: password
  data:
    redis:
      host: localhost
      port: 6379
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
server:
  port: 8080
```
You can override these via environment variables, e.g.: `SPRING_DATASOURCE_URL`, `REDIS_HOST`, etc.

## Running with Docker Compose
Start the full stack (PostgreSQL, Redis, and the application):

```bash
docker-compose up --build
```
Access the service at `http://localhost:8080`

## API Usage

### Create Short URL
- **Endpoint**: `POST /url`
- **Headers**:
  - `Content-Type: application/json`
  - `x-user-id: <userId>`
- **Body**:
```bash
{ "url": "https://www.example.com/path" }
```
- **Response**: 200 OK
  ```bash
  http://short.ly/AbCd12
  ```
### Redirect to Long URL
- **Endpoint**: `GET /url/{hash}`
- **Headers**:
  - `x-user-id: <userId>`
- **Example**:
  ```bash
  GET /url/AbCd12
  ```
- **Response**: 302 Found with `Location` header pointing to the original URL
  
## Testing
The project includes unit and integration tests (Testcontainers for PostgreSQL and Redis). Run:

```bash
./gradlew test
```

## Testing
This project is licensed under the MIT License
