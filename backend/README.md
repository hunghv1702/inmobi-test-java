# InMobi Test Java - Guess Number Game API

REST API for a number guessing game built with Java 21 and Spring Boot. The implementation focuses on clean API contracts, JWT security, transaction boundaries, and correct behavior under concurrent `/api/v1/guess` requests.

## Tech Stack

- Java 21
- Spring Boot 4.0.0
- Spring Web MVC
- Spring Security with JWT
- Spring Data JPA
- Liquibase changelog migrations
- H2 for local default runtime
- PostgreSQL profile with Docker Compose
- JUnit 5 and MockMvc
- OpenAPI Generator for API interfaces and request/response models
- Lombok for constructor/getter boilerplate reduction
- Stripe Test Mode payment integration through Spring `RestClient`

## Requirements

- Java 21+
- Docker and Docker Compose if you want to run PostgreSQL locally
- Maven is optional because the project includes Maven Wrapper
- Stripe test secret key if you want to run payment APIs against Stripe sandbox

## Run Tests

```bash
./mvnw clean verify
```

The project includes `.mvn/settings.xml` and `.mvn/maven.config`, so Maven Wrapper resolves dependencies from Maven Central for this repository using repo-local settings.

## Run Locally With H2

```bash
STRIPE_SECRET_KEY=sk_test_xxx ./mvnw spring-boot:run
```

`STRIPE_SECRET_KEY` is required only when calling payment APIs. The rest of the game APIs can run without it.

The API runs at:

```text
http://localhost:8080
```

H2 console:

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:guess_game
Username: sa
Password: <empty>
```

## Run Locally With PostgreSQL

```bash
docker compose up -d
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

Default PostgreSQL settings:

```text
Database: inmobi_test_java
Username: postgres
Password: postgres
Port: 5440
```

You can override them with:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5440/inmobi_test_java DATABASE_USERNAME=postgres DATABASE_PASSWORD=postgres SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

## Authentication Flow

Register:

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register   -H 'Content-Type: application/json'   -d '{"email":"player@example.com","password":"secret123"}'
```

Login:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login   -H 'Content-Type: application/json'   -d '{"email":"player@example.com","password":"secret123"}'
```

Use the `accessToken` value as a Bearer token:

```bash
TOKEN=<accessToken>
```

## API Examples

Get current user:

```bash
curl http://localhost:8080/api/v1/me   -H "Authorization: Bearer $TOKEN"
```

Guess a number from 1 to 5:

```bash
curl -X POST http://localhost:8080/api/v1/guess   -H "Authorization: Bearer $TOKEN"   -H 'Content-Type: application/json'   -d '{"number":3}'
```

Leaderboard:

```bash
curl http://localhost:8080/api/v1/leaderboard   -H "Authorization: Bearer $TOKEN"
```

Buy 5 turns directly (for testing without Stripe):

```bash
curl -X POST http://localhost:8080/api/v1/buy-turns   -H "Authorization: Bearer $TOKEN"
```

## Stripe Test Payment Flow

This project uses Stripe Test Mode instead of an in-memory payment mock in main source. Use a Stripe test secret key from the Stripe Dashboard:

```bash
export STRIPE_SECRET_KEY=sk_test_xxx
```

Create a checkout session for the UI:

```bash
curl -s -X POST http://localhost:8080/api/v1/payments/turn-packages/checkout \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"successUrl":"http://localhost:5173/payment/success?session_id={CHECKOUT_SESSION_ID}","cancelUrl":"http://localhost:5173/payment/cancel"}'
```

The response contains `data.checkoutUrl`. Redirect the browser to that URL, pay in Stripe sandbox, then read `session_id` from the success URL and confirm it:

```bash
curl -X POST http://localhost:8080/api/v1/payments/turn-packages/confirm \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"checkoutSessionId":"cs_test_xxx"}'
```

After Stripe reports the Checkout Session as `complete` and `paid`, the backend grants 5 turns exactly once. Stripe's common successful test card is `4242 4242 4242 4242` with any future expiry and any CVC.


## Response Format

All API responses use the same envelope:

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

For validation errors, `data.violations` contains field-level validation messages. Business and authentication errors return `data: null`.

## API Contract

The OpenAPI contract is available in:

```text
src/main/resources/openapi.yaml
```

Generated API interfaces, request/response models, and typed API response wrappers are created during Maven build under `target/generated-sources/openapi`. Controllers implement those interfaces, so route mappings, media types, validation annotations, and operation documentation come from the OpenAPI contract.

## Design Notes

- API authentication is stateless JWT.
- Passwords are stored as BCrypt hashes, never plain text.
- Source follows a layer-based BFF-style layout: `controller`, `service`, `service/impl`, `mapper`, `entity`, `repository`, `utils`, `constant`, and `exception`.
- Controllers implement OpenAPI-generated `*Api` interfaces and only handle HTTP concerns; business logic lives in services.
- API request/response classes and typed response wrappers are generated from `openapi.yaml`; JPA entities are not returned directly.
- MapStruct handles entity-to-generated-response mapping; Lombok is used for constructor injection and boilerplate reduction in handwritten code.
- Payment APIs use `StripePaymentAdapter`, which calls Stripe Test Mode REST APIs. Main source no longer uses an in-memory payment mock.
- `/api/v1/guess` and payment confirmation use transactions plus row locking where state changes must be protected.
- The lock prevents race conditions where parallel guesses could consume the same turn or make `turns` negative.
- Controllers resolve the authenticated user through `SecurityUtils.getCurrentUserId()`.
- `/api/v1/me` reads by primary key from the JWT principal.
- `/api/v1/leaderboard` returns only top 10 rows and uses the `idx_users_score_id` index.
- Responses keep the same `code`, `message`, and `data` shape, while OpenAPI exposes typed wrappers such as `LoginApiResponse`, `GuessApiResponse`, and `LeaderboardApiResponse`.

## Main Endpoints

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/me
POST /api/v1/guess
GET  /api/v1/leaderboard
POST /api/v1/buy-turns
POST /api/v1/payments/turn-packages/checkout
POST /api/v1/payments/turn-packages/confirm
```
