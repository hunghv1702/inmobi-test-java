# Guess Royale - Fullstack Java Enterprise Architecture

A complete number guessing game (1–5) designed to showcase enterprise Java Spring Boot 3 & a modern React + TypeScript frontend.

The project fulfills all requirements specified in the coding assessment (including guessing game loops, buy turns, leaderboards, stateless JWT security, concurrency protection, performance tuning under heavy loads, custom probability win rates, and sandboxed Stripe payment integration).

---

## 📁 Repository Structure

```text
inmobi-test-java/
├── backend/                  # Java Spring Boot 3 REST API Server
│   ├── src/                  # Application & Test source code
│   ├── pom.xml               # Maven configuration (Liquibase, Security, MapStruct, OpenAPI)
│   ├── docker-compose.yml    # PostgreSQL DB & Adminer services
│   ├── .env                  # Backend environment configuration
│   └── README.md             # Backend detailed technical documentation
│
└── frontend/                 # React + TypeScript + Vite + Tailwind CSS Frontend
    ├── src/                  # Components, Pages, Axios Client, API Types
    ├── .env                  # Frontend environment configuration (VITE_API_BASE_URL)
    └── package.json          # Node dependencies
```

---

## ⚙️ 1. Environment Setup

Ensure the following tools are installed on your machine:

1.  **Java Development Kit (JDK) 21 or newer** (JDK 21/24 recommended).
2.  **Node.js (v18.x or newer)** and **npm (v9.x or newer)** to run the Frontend.
3.  **Docker & Docker Compose** (Optional - only if running a local PostgreSQL database; otherwise, the backend defaults to using an in-memory H2 database).
4.  **Stripe CLI** (Optional - if you want to forward Stripe webhooks locally for sandboxed checkout testing).

---

## ⚡ 2. How to Build & Run Locally

### Step 1: Run the Backend (Spring Boot)

Open a terminal at the project root directory:

```bash
cd backend

# Option A (Default): Run with in-memory H2 database (No local setup required)
./mvnw spring-boot:run

# Option B: Run with PostgreSQL database (Docker required)
# 1. Start the PostgreSQL container:
docker-compose up -d
# 2. Run the Spring Boot backend with the postgres active profile:
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

*The backend server runs at:* `http://localhost:8080`

### Step 2: Run the Frontend (React)

Open a new terminal window at the project root directory:

```bash
cd frontend

# 1. Install dependencies:
npm install

# 2. Start the Vite development server:
npm run dev
```

*The frontend application runs at:* `http://localhost:5173`. Open this URL in your web browser to play the game!

---

## 🔐 3. Authentication & Obtaining JWT Token

The API uses **Stateless JWT (JSON Web Token)** authentication. To test endpoints using Postman or cURL, follow these steps:

### 1. Register a New Account (`POST /api/v1/auth/register`)
Provide an email address and a password (email formats are validated via regex):
```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"player@gmail.com","password":"secret123"}'
```

### 2. Login to Obtain Tokens (`POST /api/v1/auth/login`)
Log in to receive an `accessToken` and a `refreshToken`:
```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"player@gmail.com","password":"secret123"}'
```
**Successful response structure:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "766a5068-...",
    "tokenType": "Bearer"
  }
}
```
Copy the value of the `accessToken` and use it in the headers of subsequent requests:
`Authorization: Bearer <your_accessToken_string>`

---

## 📡 4. Quick API Testing (Using cURL)

Define the token as a variable in your terminal to easily test the following commands:
```bash
# On Linux/macOS or Git Bash:
export TOKEN="your_accessToken_string_here"
```

### 1. Get Current User Profile (`GET /api/v1/me`)
Retrieves the logged-in user's email, turns left, and score:
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/me
```

### 2. Submit a Guess Number (`POST /api/v1/guess`)
Submit a guess between 1 and 5 (Server rolls a secret number between 1-5 with custom win probability):
```bash
curl -X POST http://localhost:8080/api/v1/guess \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"number":3}'
```

### 3. Retrieve Global Top 10 Leaderboard (`GET /api/v1/leaderboard`)
List the top 10 users ranked by score:
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/leaderboard
```

### 4. Buy Turns Directly (`POST /api/v1/buy-turns`)
Directly add 5 turns for testing without Stripe checkout redirect flow:
```bash
curl -X POST http://localhost:8080/api/v1/buy-turns \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Create Stripe Checkout Session (`POST /api/v1/payments/turn-packages/checkout`)
Initiate a Stripe checkout session:
```bash
curl -X POST http://localhost:8080/api/v1/payments/turn-packages/checkout \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"successUrl":"http://localhost:5173/payment/success?session_id={CHECKOUT_SESSION_ID}","cancelUrl":"http://localhost:5173/payment/cancel"}'
```
