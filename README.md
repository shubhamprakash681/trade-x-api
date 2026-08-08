# TradeX – Real-Time Paper Trading Platform

## Project Overview

TradeX is a production-inspired full-stack paper trading platform built using **Java (Spring Boot)** and **React**. The application simulates a modern stock trading platform similar to Groww or INDmoney without performing any real financial transactions.

Unlike traditional demo trading applications, TradeX is **completely self-contained** and does not rely on any third-party market data providers. Instead, it contains its own market simulation engine capable of generating realistic historical and live stock market data.

This approach ensures that the project remains fully reproducible, works completely offline after setup, and is not affected by API limits, pricing changes, or external service outages.

The application demonstrates modern backend architecture using Spring Boot, Kafka, Redis, PostgreSQL, WebSockets, Docker, and React while showcasing concepts commonly used in production fintech systems.

---

# Project Objectives

- Build a production-grade paper trading platform.
- Demonstrate event-driven architecture using Kafka.
- Simulate realistic stock market behaviour.
- Implement secure authentication using JWT.
- Visualize live stock prices using WebSockets.
- Manage portfolios, watchlists, and virtual trading.
- Follow production engineering best practices.

---

# Project Scope

## Included

- User authentication
- Virtual trading
- Portfolio management
- Transaction history
- Watchlists
- Price alerts
- Real-time market streaming
- Historical stock charts
- Kafka event streaming
- Redis caching
- Responsive React frontend

## Excluded

- Real brokerage integration
- Real money transactions
- KYC
- Banking integration
- Payment gateway
- Third-party market data APIs

---

# Market Simulation Engine

One of the primary goals of TradeX is to remain completely independent of external market data providers.

Instead of consuming live stock prices from third-party APIs, the application generates and maintains its own market data.

The simulation consists of three major components.

## 1. Historical Data Seeder

During application startup, the Market Service verifies that historical data exists for every supported stock and ETF.

If any historical data is missing, a background seeding process generates realistic historical market data before the Market Service becomes available.

Characteristics

- Generates approximately 10 years of daily OHLCV candles
- Generates data for approximately 15 Indian stocks and ETFs
- Uses deterministic random seeds so generated data remains consistent
- Produces realistic long-term bullish and bearish trends
- Simulates market crashes and recoveries
- Generates realistic daily trading volumes

Example symbols

- RELIANCE
- TCS
- INFY
- HDFCBANK
- ICICIBANK
- SBIN
- ITC
- LT
- AXISBANK
- BHARTIARTL
- MARUTI
- TITAN
- ASIANPAINT
- NIFTYBEES
- BANKBEES

---

## 2. Startup Validation

Every time the Market Service starts:

1. Check every supported symbol.
2. Verify that historical data exists from the configured start date until today.
3. Generate only the missing dates.
4. Store generated candles in PostgreSQL.
5. Start the REST APIs only after validation completes.

This guarantees that the database is always complete and consistent.

---

## 3. Live Market Simulator

After startup, a Market Simulator continuously generates live market ticks based on the latest available historical candle.

The simulator uses stochastic price movement algorithms such as:

- Random Walk
- Geometric Brownian Motion
- Mean Reversion
- Volume Simulation

Generated market events are published to Kafka.

Example event

```json
{
  "symbol": "NIFTYBEES",
  "price": 284.72,
  "volume": 1245,
  "timestamp": 1785307200
}
```

These events are consumed by the WebSocket service and streamed to connected clients in real time.

---

# Technology Stack

## Backend

- Java 21
- Spring Boot 3
- Spring Security
- JWT Authentication
- Spring Data JPA
- PostgreSQL
- Kafka
- Redis
- WebSockets
- Flyway
- Maven

## Frontend

- React
- TypeScript
- Redux Toolkit
- React Query
- React Router
- Tailwind CSS
- TradingView Lightweight Charts

## Infrastructure

- Docker
- Docker Compose

---

# Milestone 1 – Foundation

## Objective

Build authentication, user management, and stock catalog.

### Features

- User registration
- Login
- JWT authentication
- Refresh tokens
- Profile management
- Seed supported stock master

### REST APIs
- POST /api/auth/signup
- POST /api/auth/login
- POST /api/auth/refresh
- POST /api/auth/logout
- GET /api/users/me
- PUT /api/users/me
- PUT /api/users/password
- GET /api/stocks
- GET /api/stocks/{symbol}
- GET /api/stocks/search

---

# Milestone 2 – Historical Market Generation

## Objective

Create a deterministic market data generation engine.

### Features
- Generate 10 years of historical data
- Generate OHLCV candles
- Generate realistic market trends
- Startup validation
- Automatic regeneration of missing dates

### REST APIs
- GET /api/market/history/{symbol}
- GET /api/market/candle/{symbol}
- POST /api/admin/market/regenerate
- GET /api/admin/market/status

---

# Milestone 3 – Paper Trading

## Objective

Implement portfolio management and paper trading.

### Features
- ₹10,00,000 virtual balance
- Buy stocks
- Sell stocks
- Portfolio summary
- Holdings
- Transaction history

### REST APIs
- GET /portfolio
- GET /portfolio/summary
- GET /portfolio/holdings
- POST /orders/buy
- POST /orders/sell
- GET /orders/history
- GET /transactions

---

# Milestone 4 – Live Market Streaming

## Objective

Generate live simulated market prices.

### Features

- Kafka Producer
- Kafka Consumer
- Redis Cache
- Live Tick Generator
- WebSocket Streaming

### REST APIs
- GET /prices/latest
- GET /prices/{symbol}
- GET /prices/history

### WebSocket

- /ws
- /topic/market
- /topic/{symbol}

---

# Milestone 5 – Advanced Features

## Features

- Watchlists
- Price alerts
- Notifications
- Redis caching
- Dashboard
- Top gainers
- Top losers
- Trending stocks (derived from simulated market)

### REST APIs

- GET /watchlist
- POST /watchlist
- DELETE /watchlist/{symbol}
- POST /alerts
- GET /alerts
- DELETE /alerts
- GET /notifications
- GET /market/gainers
- GET /market/losers
- GET /market/trending

---

# Milestone 6 – Production Readiness

## Features

- Docker Compose
- GitHub Actions
- Prometheus
- Grafana
- OpenTelemetry
- Integration Tests
- Testcontainers
- Comprehensive Documentation

---

# Suggested Repository Structure

```
tradex/
├── auth-service/
├── market-service/
├── portfolio-service/
├── order-service/
├── notification-service/
├── websocket-service/
├── common-lib/
├── frontend-react/
├── docker-compose.yml
└── README.md
```

---

# Design Goals

- Fully self-contained
- No external API dependency
- Deterministic market simulation
- Event-driven architecture
- Production-inspired codebase
- Interview-ready system design
- Easy local setup with Docker Compose