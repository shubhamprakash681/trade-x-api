# TradeX – Real-Time Paper Trading Platform

## Project Description

TradeX is a production-inspired full-stack paper trading platform built using **Java (Spring Boot)** and **React**. It is designed as a showcase application that mimics the user experience of modern investment platforms such as Groww and INDmoney without executing any real financial transactions.

The application allows users to register, manage a virtual portfolio, buy and sell stocks using virtual currency, create watchlists, and visualize live market data through interactive charts. It integrates with third-party market data APIs for selected real instruments (such as NIFTYBEES) while also generating synthetic market data (for example, **SNIFTYBEES**) using Kafka-based event streaming. This demonstrates both external data integration and event-driven architecture.

The project is intended to follow production-grade software engineering practices, including modular architecture, RESTful APIs, JWT authentication, WebSockets, Redis caching, Kafka messaging, Docker-based deployment, automated testing, and observability.

## Project Objectives

- Build a scalable fintech-style web application.
- Demonstrate microservice-ready architecture using Spring Boot.
- Implement secure authentication and authorization.
- Simulate paper trading with virtual money.
- Stream real-time market prices using Kafka and WebSockets.
- Integrate external stock market APIs.
- Showcase modern frontend development with React and TypeScript.
- Apply production engineering practices such as Docker, CI/CD, monitoring, testing, and documentation.

## Scope

### Included
- User authentication
- Stock search
- Portfolio management
- Paper trading
- Watchlist
- Real-time charts
- Kafka-based synthetic price generation
- Real market data integration
- Notifications and price alerts
- Responsive web UI

### Excluded
- Real brokerage integration
- Real money transactions
- KYC
- Payment gateway
- Banking integrations

---

# Milestone-wise Requirements

## Milestone 1 – Foundation
### Objective
Build authentication, user management, and stock catalog.

### APIs
- POST /api/auth/signup
- POST /api/auth/login
- POST /api/auth/refresh
- POST /api/auth/logout
- GET /api/users/me
- PUT /api/users/me
- PUT /api/users/password
- GET /api/stocks
- GET /api/stocks/{symbol}
- GET /api/stocks/search?q=

## Milestone 2 – Paper Trading
### Objective
Implement virtual trading and portfolio management.

### APIs
- GET /portfolio
- GET /portfolio/summary
- GET /portfolio/holdings
- POST /orders/buy
- POST /orders/sell
- GET /orders/history
- GET /transactions

## Milestone 3 – Real-Time Streaming
### Objective
Introduce Kafka, Redis, and WebSocket streaming.

### APIs
- GET /prices/latest
- GET /prices/{symbol}
- GET /prices/history

### WebSocket Topics
- /ws
- /topic/market
- /topic/{symbol}

## Milestone 4 – External Market Integration
### Objective
Consume third-party market APIs and publish prices to Kafka.

### APIs
- GET /market/indices
- GET /market/gainers
- GET /market/losers
- GET /market/trending

## Milestone 5 – Advanced Product Features
### Objective
Implement watchlists, alerts, notifications, and caching.

### APIs
- GET /watchlist
- POST /watchlist
- DELETE /watchlist/{symbol}
- POST /alerts
- GET /alerts
- DELETE /alerts
- GET /notifications

## Milestone 6 – Production Readiness
### Objective
Prepare the application for production-style deployment.

Features include Docker, Prometheus, Grafana, OpenTelemetry, GitHub Actions, automated testing, and comprehensive documentation.

## Suggested Repository Structure

```text
tradex/
├── auth-service/
├── market-service/
├── portfolio-service/
├── order-service/
├── price-stream-service/
├── websocket-service/
├── notification-service/
├── common-lib/
├── frontend-react/
├── docker-compose.yml
└── README.md
```

---

# Implementation Status

## Completed: Milestone 1 – Foundation

This repository is now scaffolded as a Maven multi-module Spring Boot microservice system:

- `common-lib` – shared JWT utilities, JWT properties, principal model, and API error shape.
- `config-server` – centralized Spring Cloud Config Server running on port `8888`, backed by `config-server/src/main/resources/config-repo`.
- `discovery-server` – Eureka service registry running on port `8761`.
- `api-gateway` – Spring Cloud Gateway running on port `8080`, with Java-based routes and a Java `GlobalFilter` that validates JWTs before forwarding protected `/api/**` traffic.
- `auth-service` – authentication and user profile service running on port `8081`.
- `market-service` – stock catalog/search service running on port `8082`.

## Completed: Milestone 2 – Paper Trading

- `portfolio-service` – virtual portfolio, holdings, paper buy/sell orders, and transaction ledger service running on port `8083`.
- Users receive a virtual cash account on first portfolio access.
- Buy and sell orders are executed immediately using the current `market-service` stock `referencePrice`.
- Holdings track quantity, average price, market value, and unrealized P/L.
- Order history and transaction history are stored per authenticated user.

## Milestone 1 APIs

Gateway base URL: `http://localhost:8080`

Auth and user APIs:

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `PUT /api/users/me`
- `PUT /api/users/password`

Stock APIs:

- `GET /api/stocks`
- `GET /api/stocks/{symbol}`
- `GET /api/stocks/search?q=`

Paper trading APIs:

- `GET /portfolio`
- `GET /portfolio/summary`
- `GET /portfolio/holdings`
- `POST /orders/buy`
- `POST /orders/sell`
- `GET /orders/history`
- `GET /transactions`

## API Documentation

Swagger UI is enabled from the first milestone:

- Gateway Swagger UI: `http://localhost:8080/swagger-ui.html`
- Auth service OpenAPI: `http://localhost:8080/auth/v3/api-docs`
- Market service OpenAPI: `http://localhost:8080/market/v3/api-docs`
- Portfolio service OpenAPI: `http://localhost:8080/portfolio/v3/api-docs`
- Eureka dashboard: `http://localhost:8761`
- Config Server example: `http://localhost:8888/api-gateway/default`

## Local Run Commands

Start PostgreSQL first. The Spring services read database settings from the Config Server, which resolves them from `.env`.

```bash
docker compose up -d postgres_container pgadmin_container
```

Use the same `TRADEX_JWT_SECRET` value for all services. For local runs, `POSTGRES_HOST=localhost` and `POSTGRES_PORT=5431` match the Compose port mapping.

```bash
set -a
source .env
set +a
mvn -pl config-server spring-boot:run
mvn -pl discovery-server spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl market-service spring-boot:run
mvn -pl portfolio-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

Run the full build:

```bash
mvn test
```
