# ॐ ShubhaSamagri — Pooja Essentials Ecommerce

A production-ready full-stack ecommerce application for authentic Hindu ritual kits and pooja essentials, curated by temple poojaris for sacred occasions.

## Features

- Browse occasions (Marriage, Gruha Pravesh, Satyanarayana Vratham, etc.)
- View curated pooja kits with complete item lists
- Add to cart, update quantities, remove items
- JWT-based login/signup
- Place orders with delivery details
- Track order status and history
- Swagger UI for API documentation

## Tech Stack

| Layer    | Technology                   |
|----------|------------------------------|
| Frontend | React 18, Vite, React Router |
| Backend  | Spring Boot 3.2, Java 17     |
| Security | Spring Security + JWT (JJWT) |
| Database | H2 (dev) / MySQL (prod)      |
| API Docs | SpringDoc OpenAPI / Swagger  |

## Quick Start

### Backend
```bash
cd backend
mvn spring-boot:run
```
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

### Frontend
```bash
cd frontend
npm install
npm run dev
```
- App: http://localhost:5173

### Test Credentials
| Role  | Email                        | Password    |
|-------|------------------------------|-------------|
| Admin | admin@shubhasamagri.com      | admin123    |
| User  | ravi@example.com             | password123 |

## Project Structure
```
ShubhaSamagri/
├── backend/          Spring Boot API
├── frontend/         React SPA
├── docs/             Architecture, Deployment, Interview Guide
└── postman/          API test collection
```

## Sample Occasions & Kits

| Occasion              | Kit Name                          | Price    |
|-----------------------|-----------------------------------|----------|
| Marriage              | Complete Marriage Pooja Kit       | ₹2,499   |
| Gruha Pravesh         | Gruha Pravesh Starter Kit         | ₹1,299   |
| Satyanarayana Vratham | Satyanarayana Puja Kit            | ₹899     |
| Naming Ceremony       | Namakarana (Naming Ceremony) Kit  | ₹699     |
| Upanayanam            | Upanayanam (Sacred Thread) Kit    | ₹1,599   |

## API Endpoints

| Method | Endpoint               | Auth    | Description          |
|--------|------------------------|---------|----------------------|
| POST   | /api/auth/signup       | Public  | Register user        |
| POST   | /api/auth/login        | Public  | Login, get JWT       |
| GET    | /api/occasions         | Public  | Browse occasions     |
| GET    | /api/kits/occasion/:id | Public  | Kits by occasion     |
| GET    | /api/kits/:id          | Public  | Kit details          |
| POST   | /api/cart/add          | JWT     | Add to cart          |
| POST   | /api/orders            | JWT     | Place order          |
| GET    | /api/orders            | JWT     | My orders            |

## Deployment

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for step-by-step guide to deploy FREE on:
- Backend: **Render.com**
- Frontend: **Netlify**
- Database: **Railway** (MySQL)

## Documentation

- [Architecture & Database Schema](docs/ARCHITECTURE.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [Interview Guide](docs/INTERVIEW_GUIDE.md)
- Import [postman/ShubhaSamagri.postman_collection.json](postman/ShubhaSamagri.postman_collection.json) to test all APIs
