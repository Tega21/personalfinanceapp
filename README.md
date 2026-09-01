# Personal Finance Application

A full-stack personal finance web application built as a senior capstone project for Grand Canyon University (CST-451 / CST-452). It prioritizes privacy, so no need to link bank accounts.

---

## Overview

The Personal Finance Application helps users take control of their finances through intentional, manual tracking — with **no bank account linking** and **no third-party data sharing**. Users log their own income and expenses, set budgets, track multiple accounts, and forecast upcoming cash flow, all while keeping their financial data completely private.

The project was built across three sprints:

- **Sprint 1** — User authentication, transaction management, categories, and a monthly dashboard
- **Sprint 2** — Budgets, spending trends, transaction filtering/search, and user profile
- **Sprint 3** — Multi-account tracking, net worth, recurring transactions, and cash flow forecasting

---

## Features

- Secure user registration and login with JWT authentication
- Manual transaction entry (add, edit, delete) with 15 default categories plus custom categories
- Monthly dashboard with income/expense totals, net cash flow, and a category breakdown pie chart
- Monthly budgets per category with color-coded progress (OK / WARNING / EXCEEDED)
- Six-month spending trend line chart
- Transaction filtering by category, date range, and live keyword search
- User profile management (name, email, password)
- Multiple financial account tracking (checking, savings, other) with total net worth
- Recurring transaction templates for bills and income
- 30/60/90-day cash flow forecast based on recurring transactions

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.3.5, Spring Security (JWT), Spring Data JPA / Hibernate |
| Frontend | React 19, TypeScript, Vite, Recharts |
| Database | PostgreSQL |
| Build | Maven |
| Tools | IntelliJ IDEA, Postman, pgAdmin 4 |

---

## Prerequisites

Before running the application, make sure you have installed:

- **Java 21** (JDK)
- **Node.js 20+** and npm
- **PostgreSQL 15+**
- **Maven** (or use the included Maven wrapper `mvnw`)

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/Tega21/personalfinanceapp.git
cd personalfinanceapp
```

### 2. Create the PostgreSQL database

Using pgAdmin or the `psql` command line, create a database named `personalfinance`:

```sql
CREATE DATABASE personalfinance;
```

### 3. Configure environment variables

The application reads sensitive configuration from a `.env` file in the project root (loaded via spring-dotenv). This file is **not committed to version control** for security.

Create a file named `.env` in the project root with your own values:

```
DB_USERNAME=your_postgres_username
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_own_long_random_secret_key
```

A `.env.example` file is included in the repository as a template — copy it and fill in your own values:

```bash
cp .env.example .env
```

> **Never commit your real `.env` file.** It is listed in `.gitignore` so your credentials stay on your machine only.

### 4. Run the backend

From the project root, either run through your IDE (IntelliJ: run `PersonalfinancetrackerApplication`) or from the command line using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

The backend starts on **http://localhost:8080**. On first launch, Hibernate automatically creates all database tables from the JPA entity definitions.

### 5. Run the frontend

In a separate terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:3000**.

### 6. Open the application

Navigate to **http://localhost:3000** in your browser, register a new account, and start tracking your finances.

---

## Running Tests

The backend includes JUnit unit tests (using Mockito) and integration tests (using `@SpringBootTest` + MockMvc against a real database).

Run all tests with:

```bash
./mvnw test
```

Test coverage includes:

- **Unit tests:** AuthService, CategoryService, DashboardService, TransactionService, BudgetService, AccountService, RecurringTransactionService, ForecastService
- **Integration tests:** end-to-end authentication and endpoint validation


---

## Architecture

The application follows a standard three-tier architecture:

1. **Presentation Layer** — React SPA (port 3000) handles all UI and communicates with the backend via REST/JSON over HTTP.
2. **Business Logic Layer** — Spring Boot REST API (port 8080) enforces business rules, validation, and authentication. Follows a Controller → Service → Repository pattern.
3. **Data Persistence Layer** — PostgreSQL (port 5432) stores all application data with referential integrity enforced through foreign key constraints.

Authentication is stateless using JWT tokens. Passwords are hashed with BCrypt. All data queries are scoped to the authenticated user's ID, enforcing complete per-user data isolation.

---

## Author

**Brandon Ortega**
Grand Canyon University — College of Engineering & Technology
CST-451 / CST-452 Senior Capstone
brandon.ortega19@my.gcu.edu

---

## License

This project was developed for academic purposes as part of the GCU Computer Science program.
