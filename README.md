# Guess Royale - Fullstack Java Middle Architecture

A fullstack gaming application designed to showcase enterprise Java Spring Boot 3 architecture and a modern React + TypeScript Frontend interface.

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

## ⚡ How to Run Locally

### 1. Start Backend (Java Spring Boot)

```bash
cd backend

# Start PostgreSQL database (Optional - defaults to H2 in-memory DB if PostgreSQL is off)
docker-compose up -d

# Run Spring Boot backend server (runs on http://localhost:8080)
./mvnw spring-boot:run
```

### 2. Start Frontend (React + Vite)

```bash
cd frontend

# Install dependencies
npm install

# Start Vite development server (runs on http://localhost:5173)
npm run dev
```

Open your browser at **`http://localhost:5173`** to access the game UI!

---

## 🛠️ Architecture Highlights

### Backend (`/backend`)
* **Framework**: Spring Boot 3 (Java 21)
* **API Architecture**: OpenAPI 3.0 Contract-First (`openapi.yaml` -> Generated Interfaces)
* **Database Migrations**: Liquibase (`src/main/resources/db/changelog/master.xml`)
* **Security**: Stateless JWT Auth + BCrypt Password Encoder
* **Payment Integration**: Stripe Hosted Checkout Session & Webhook fulfillment
* **Concurrency Protection**: Optimistic locking (`@Version`) + Row-level pessimistic locking (`FOR UPDATE`)

### Frontend (`/frontend`)
* **Framework**: React 19 + TypeScript + Vite
* **Styling**: Tailwind CSS v4 + Cyberpunk Neon Glassmorphism Theme
* **HTTP Client**: Centralized Axios with JWT Request Interceptor & Envelope Response Parsing
* **Type Safety**: TypeScript Interfaces matching 1-1 with Backend OpenAPI DTOs
* **User Experience**: Interactive 1-5 Number Picker, Slot Machine Reel Spin, Confetti Explosion on Win, Leaderboard & Stripe Payment Redirects
