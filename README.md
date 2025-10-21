# CCEP MACH Microservice Exercise



## Candidate Name

**Candidate Name:** D Visal kumar Rao


---


## Objective
Design and implement a simple microservice that adheres to **MACH architecture principles**:
- **Microservices**: Independently deployable services.
- **API-first**: All functionality exposed via APIs.
- **Cloud-native**: Designed for cloud environments.
- **Headless**: Decoupled frontend and backend.

The microservice should expose a RESTful API to manage a resource (for example, a **health goal** for consumers).

---

## Requirements


### 1. Design the Microservice
- Define the resource your service will manage (e.g., health goals, user profiles, tasks). For example, you may choose to manage a `health goal` resource.
- Create a **brief architectural diagram** showing how your service fits into a MACH ecosystem and potential interactions with other services.  
  > You may use Mermaid.js, draw.io, or any tool of your choice. Include the diagram (or link) in your repo, preferably in a `/docs` folder or directly in the README.


### 2. Implement the Microservice
- Choose a programming language and framework:
  - **Java** (Spring Boot)
  - **Python** (Flask or FastAPI)
  - **JavaScript/TypeScript** (Node.js with Express or NestJS)
- Implement a RESTful API with endpoints for:
  - `POST /resource` → Create a new resource
  - `GET /resource` → Retrieve all resources
  - `PUT /resource/{id}` → Update a resource
  - `DELETE /resource/{id}` → Delete a resource
- Include:
  - Input validation
  - Error handling


### 3. Documentation
Add clear documentation in your repo that explains:
- How to set up and run the service locally (use code blocks for commands).
- The design choices you made, including any assumptions.
- How your service adheres to MACH principles.

**Recommended Documentation Structure:**
- Setup
- Usage
- Design Decisions
- MACH Principles
- API Reference
- Testing


### 4. Testing
- Write **unit tests** for your API endpoints.  
- Provide instructions on how to run the tests.  


### 5. Optional Enhancements 
- Cloud deployment instructions (AWS, Azure, GCP).  
- Simple frontend interface (e.g., React) to interact with the API. 
- Infrastructure as Code (Terraform) for provisioning.
- Briefly describe your approach to automated testing and deployment (CI/CD).


## Deliverables
- Working microservice in your chosen language/framework.  
- README documenting design and setup.  
- Unit tests.  
- Optional: diagrams, deployment/config files, frontend.  

---


## Getting Started
- [ ] Fork this repository (`ccep-mach-microservice-exercise`) into your own GitHub account
- [ ] Fill in your name at the top of this README under *Candidate Name*
- [ ] Implement your solution in the forked repository
- [ ] Update the README with your instructions and design notes
- [ ] Share the link to your completed repository

---

## Candidate Note on Evaluation
Submissions will be evaluated on correctness, code quality, documentation clarity, adherence to MACH principles, and test coverage.

---

## ⚙️ Module Responsibilities

| Module | Description |
|---------|-------------|
| **openapispec/** | Contains the canonical `openapi.yaml` contract and uses `openapi-generator-maven-plugin` to generate code (API interfaces + DTOs). |
| **api/** | Implements the generated interfaces with business logic, persistence, validation, and error handling using **Spring Boot 3.5 / Java 17**. |
| **Parent POM** | Defines shared versions, dependency management, and builds both modules together. |

---

## 📘 OpenAPI Specification

**File:** `openapispec/src/main/resources/openapi.yaml`  
**Version:** 3.0.3  
**Generator:** [OpenAPI Generator Maven Plugin](https://github.com/OpenAPITools/openapi-generator)

### Includes:
- `x-api-key`, `x-correlation-id`, and `x-request-id` headers on all endpoints
- Standard error responses (`400`, `401`, `404`, `500`, `503`, `504`)
- Reusable schemas for `HealthGoal`, `CreateHealthGoalRequest`, `UpdateHealthGoalRequest`, and `ErrorResponse`
- Jakarta Validation (`@NotBlank`, `@Size`, etc.)
- API key authentication via header `x-api-key`

### To Regenerate Code:
```bash
cd openapispec
mvn clean generate-sources
```

### Access the Service

Base URL: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI Spec: http://localhost:8080/openapi.yaml

### 🧱 Technology Stack

| **Category**       | **Technology** |
|--------------------|----------------|
| **Language**       | Java 17 |
| **Framework**      | Spring Boot 3.5.6 |
| **OpenAPI Tools**  | openapi-generator-maven-plugin 7.7.0 |
| **Validation**     | Jakarta Validation + Hibernate Validator |
| **Database**       | PostgreSQL 16 |
| **Containerization** | Docker + Docker Compose |
| **Logging**        | Logback JSON (Logstash encoder) |
| **Testing**        | JUnit 5, MockMvc |
| **Build Tool**     | Maven (multi-module) |


`curl -X GET "http://localhost:8080/bayer/v1/health-goals" \
  -H "x-api-key: api_key" \
  -H "x-correlation-id: abc123" \
  -H "x-request-id: req-001"`

`curl -X POST "http://localhost:8080/bayer/v1/health-goals" \
  -H "Content-Type: application/json" \
  -H "x-api-key: api_key" \
  -d '{
    "userId": "f8d6a25b-8a4d-4d0f-8b29-2ef14e4126a1",
    "title": "Lose Weight",
    "description": "Target to lose 5 kg in 3 months",
    "target": 5,
    "unit": "kg",
    "startDate": "2025-10-20",
    "endDate": "2026-01-20"
  }'`

`curl -X GET "http://localhost:8080/bayer/v1/health-goals/8a43a71b-56e3-4e53-9e64-8f29dcd351cd" \
-H "x-api-key: api_key" \
-H "x-correlation-id: abc123" \
-H "x-request-id: req-003"`

###
🐳 Run with Docker Compose
`docker-compose up --build`

| **Endpoint**                           | **HTTP Method** | **Description**                                        | **Request Payload**                                                                                                                                                                    | **Response**                                | **HTTP Status Codes**                           |
| -------------------------------------- | --------------- | ------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------- | ----------------------------------------------- |
| `/bayer/v1/health-goals`               | **GET**         | Retrieve all health goals for all users                | None                                                                                                                                                                                   | `200 OK` → Array of `HealthGoal` objects    | `200`, `400`, `401`, `500`, `503`, `504`        |
| `/bayer/v1/health-goals/{id}`          | **GET**         | Retrieve a specific health goal by its ID              | None                                                                                                                                                                                   | `200 OK` → Single `HealthGoal` object       | `200`, `400`, `401`, `404`, `500`, `503`, `504` |
| `/bayer/v1/health-goals`               | **POST**        | Create a new health goal for a user                    | `CreateHealthGoalRequest`<br/>`json { "userId": "uuid", "title": "string", "description": "string", "target": 5, "unit": "kg", "startDate": "YYYY-MM-DD", "endDate": "YYYY-MM-DD" }`   | `201 Created` → Created `HealthGoal` object | `201`, `400`, `401`, `500`, `503`, `504`        |
| `/bayer/v1/health-goals/{id}`          | **PUT**         | Update an existing health goal                         | `UpdateHealthGoalRequest`<br/>`json { "title": "string", "description": "string", "target": 6, "unit": "kg", "startDate": "YYYY-MM-DD", "endDate": "YYYY-MM-DD", "status": "ACTIVE" }` | `200 OK` → Updated `HealthGoal` object      | `200`, `400`, `401`, `404`, `500`, `503`, `504` |
| `/bayer/v1/health-goals/{id}`          | **DELETE**      | Delete a health goal by ID                             | None                                                                                                                                                                                   | `204 No Content`                            | `204`, `400`, `401`, `404`, `500`, `503`, `504` |
| `/bayer/v1/health-goals/user/{userId}` | **GET**         | Retrieve all health goals belonging to a specific user | None                                                                                                                                                                                   | `200 OK` → Array of `HealthGoal` objects    | `200`, `400`, `401`, `404`, `500`, `503`, `504` |
