# DateDeals

DateDeals is a full‑stack web application for managing local deals and coupons. It includes a Spring Boot backend API and an Angular portal UI, with Docker support for local development.

## Table of contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Repository structure](#repository-structure)
- [Prerequisites](#prerequisites)
- [Environment configuration](#environment-configuration)
- [Running locally (no Docker)](#running-locally-no-docker)
- [Running with Docker](#running-with-docker)
- [API documentation](#api-documentation)
- [Testing](#testing)
- [Linting and formatting](#linting-and-formatting)
- [Building for production](#building-for-production)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Features

- Manage businesses, deals, coupons, and users.
- Admin and business workflows.
- Secure API access with authentication.
- Responsive Angular portal for end users and administrators.
- Ready for containerized deployment with Docker.

## Architecture

- **Backend**: Spring Boot REST API (Java) in [backend](backend)
- **Frontend**: Angular application in [portal](portal)
- **Reverse proxy**: Nginx configuration for the portal in [portal/nginx.conf](portal/nginx.conf)
- **Orchestration**: Docker Compose in [docker-compose.yml](docker-compose.yml)

## Tech stack

- Java + Spring Boot
- Maven
- Angular
- TypeScript
- Docker + Docker Compose
- Nginx

## Repository structure

```
.
├── backend
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   └── resources
│       └── test
├── portal
│   ├── Dockerfile
│   ├── angular.json
│   └── src
└── docker-compose.yml
```

## Prerequisites

Choose **either** local tooling or Docker:

### Local tooling

- Java 17+ (check [backend/pom.xml](backend/pom.xml) if a specific version is required)
- Maven 3.8+
- Node.js 18+ and npm
- Angular CLI (optional for running via `npm` scripts)

### Docker

- Docker Engine 24+
- Docker Compose v2

## Environment configuration

Backend configuration is defined in [backend/src/main/resources/application.properties](backend/src/main/resources/application.properties) and tests in [backend/src/test/resources/application-test.properties](backend/src/test/resources/application-test.properties).

Frontend environment values live in [portal/src/environments/environment.ts](portal/src/environments/environment.ts).

If you need to configure API base URLs, authentication secrets, or database settings, update these files or use environment variables as defined by the application.

## Running locally (no Docker)

### 1) Backend

From [backend](backend):

- Build and run:
	- `./mvnw spring-boot:run`

### 2) Frontend

From [portal](portal):

- Install dependencies:
	- `npm install`
- Start dev server:
	- `npm run start`

The portal will be available at the URL shown in the terminal, typically http://localhost:4200.

## Running with Docker

From the repository root:

- Build and start all services:
	- `docker compose up --build`

The portal will be served via Nginx as configured in [portal/nginx.conf](portal/nginx.conf). The backend will be exposed according to [docker-compose.yml](docker-compose.yml).

To stop:

- `docker compose down`

## API documentation

If Swagger/OpenAPI is enabled in the backend, it is typically available at:

- `http://localhost:8080/swagger-ui/index.html`

If this path is not available, check [backend/src/main/resources/application.properties](backend/src/main/resources/application.properties) for the configured API documentation endpoint or disablement.

## Testing

### Backend

From [backend](backend):

- Run tests:
	- `./mvnw test`

### Frontend

From [portal](portal):

- Run unit tests:
	- `npm run test`

## Linting and formatting

Frontend linting (if configured in [portal/package.json](portal/package.json)):

- `npm run lint`

Backend formatting and linting depend on the Maven plugins configured in [backend/pom.xml](backend/pom.xml).

## Building for production

### Backend

From [backend](backend):

- Build a production JAR:
	- `./mvnw clean package`

### Frontend

From [portal](portal):

- Build:
	- `npm run build`

Output will be in [portal/dist](portal/dist).

## Troubleshooting

### Port conflicts

If a service fails to start, check for ports in use and adjust [docker-compose.yml](docker-compose.yml) or your local dev server ports.

### CORS issues

If the portal cannot reach the API locally, verify CORS settings in backend configuration and confirm the frontend is pointing at the correct API base URL in [portal/src/environments/environment.ts](portal/src/environments/environment.ts).

### Database configuration

Review [backend/src/main/resources/application.properties](backend/src/main/resources/application.properties) for database connection settings and ensure required services are running.

## Contributing

1. Create a feature branch.
2. Make your changes with tests.
3. Ensure checks pass.
4. Open a pull request with a clear description.

## License

Specify the license for this repository here.