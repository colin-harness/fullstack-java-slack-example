# Slack-like Chat Application

A full-stack chat application built with React frontend and Java Spring Boot backend.

## Project Structure

```
├── frontend/          # React TypeScript application
├── backend/           # Spring Boot Java application
├── docker-compose.yml # Docker setup for development
└── README.md         # This file
```

## Prerequisites

- Node.js 18+ and npm
- Java 17+
- Maven 3.6+
- Docker (optional, for database)

## Getting Started

### Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run
```

### Frontend (React)
```bash
cd frontend
npm install
npm start
```

## Features

- Real-time messaging
- Channel-based chat
- User authentication
- Message history
- Modern responsive UI

## Tech Stack

### Frontend
- React 18 with TypeScript
- Material-UI for components
- Socket.IO for real-time communication
- React Router for navigation
- Axios for API calls

### Backend
- Spring Boot 3
- Spring Security for authentication
- Spring Data JPA for database
- WebSocket for real-time messaging
- H2/PostgreSQL database
- Maven for dependency management
