# Brokerage Asset Management Application

This is a simple MVP (Minimum Viable Product) for managing customer assets using Spring Boot.

The application provides a REST API to perform CRUD operations on customer assets, including tracking asset size and usable size.  
Authentication is handled using **JWT (JSON Web Token)**.

## Technologies Used

- **Spring Boot**
- **Spring Security (JWT-based)**
- **Spring Data JPA**
- **H2 Database**
- **Lombok**
- **Swagger (OpenAPI)**

## Project Structure

The project follows a layered (n-tier) architecture:

- **Controller**
- **Service**
- **Repository**
- **DTOs / Entities**

### Key Features

- JWT-based authentication and authorization
- Custom exception handling
- Custom authentication filter for JWT processing
- Data initialization on startup (`data.sql`)
- In-memory H2 database with a simple asset schema. The data is stored in the ASSETS,ORDERS,CUSTOMERS tables.


## Endpoints Overview

### Authentication

- `POST /api/auth/login`  
  Accepts username and password, returns a JWT token.
  Use the token in Authorization headers for protected endpoints.

### Asset Management

These endpoints are secured and require a valid JWT token in the `Authorization` header (e.g., `Bearer <token>`):

- `GET /api/customers` — List all user
- `POST /api/customers` — Create new user
- `GET /api/assets/customer/{customerId}` — Get all assets for a customer
- `GET /api/assets/customer/{customerId}/asset/{assetName}` — Get asset for a customer
- `POST /api/orders/{orderId}/complete` — Complete an order - MATCHING
- `POST /api/orders/{orderId}/cancel` — Cancel an order - CANCELED
- `POST /api/orders/create-order` — Create an order 
- `GET /api/orders/customer/{customerId}` — Get all orders for a customer 
- `GET /api/orders/customer/{customerId}/range` — Get all orders for a customer by start and end date

## Running the Application

### Requirements

- Java 17+
- Maven

### Steps

1. Clone the repository:

```bash
git clone https://github.com/osmanerturk/brokerage-api.git
cd brokerage-api
```

2. Run the application:

```bash
./mvnw spring-boot:run
```
The application will start at: http://localhost:8080

### Swagger UI
You can explore and test the APIs using Swagger UI:

```bash
http://localhost:8080/swagger-ui/index.html
```
Click "Authorize" and enter your JWT token in this format:

```bash
Bearer <your_token>
```

### Initial Data Details for Testing

| Username | Password  |
| -------- | --------- |
| admin    | admin     |
| alice    | alicepass |
| bob      | bobpass   |
| ing      | lion      |

The initial data is **populated every time when the application runs**.
The SQL script can be found in the /resources/data.sql


## H2 Database Console
The application uses an in-memory H2 database for simplicity.

You can access the H2 console at:

```bash
http://localhost:8080/h2-console
```

Settings:

JDBC URL: jdbc:h2:mem:brokerage

Username: sa

Password: sa
