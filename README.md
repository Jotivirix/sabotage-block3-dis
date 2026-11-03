# 🛠️ Lab & Defense Challenge: The Architectural Sabotage (Anti-AI Lab)
**Course:** Software Development & Integration (DSI) — Universidad Francisco de Vitoria (UFV)  
**Modules Covered:** Sessions 7, 8, and 9 (Spring Boot Layered Architecture & FastAPI Universal Equivalence)

---

## 🎯 The Mission
You have been provided with an intentionally "sabotaged" API implementation written in both **Spring Boot (Java)** and **FastAPI (Python)**.
While the code executes, it contains **severe architectural violations, broken REST semantics, and leaked domain logic**.

Generative AI models will easily write fresh code, but **they struggle to diagnose subtle architectural boundaries in existing code without creating massive regressions**. Your task is to diagnose, refactor, and verbally defend your design.

---

## 🔍 The 3 Sabotaged Smells to Hunt & Refactor

### Smell 1: The Bypassed Service & Leaked Persistence
* **Violation:** The `EnrollmentController` directly interacts with the file system / repositories.
* **Refactoring Requirement:** Restore strict layered hierarchy: `Controller -> Service -> Repository -> Model/DTO`. The Controller must only deal with HTTP contracts.

### Smell 2: Missing Referential Integrity & Wrong HTTP Codes
* **Violation:** The endpoint accepts enrollments for students or subjects that do NOT exist. When it detects an error, it responds with `200 OK` containing `{"error": "not found"}`.
* **Refactoring Requirement:** 
  * Validate foreign references (`student_id`, `subject_id`) in the Service layer.
  * If the foreign entity is absent, reject with **400 Bad Request**.
  * If the student is already enrolled in the subject, reject with **409 Conflict**.
  * When a resource is successfully created, return **201 Created** with the `Location` header pointing to the new resource URI.

### Smell 3: Declarative Validation Misplacement
* **Violation:** Negative or invalid grades (e.g. `grade = -5.0` or `grade = 12.0`) are checked through manual `if` statements scattered across endpoints instead of declarative contracts.
* **Refactoring Requirement:**
  * In Java: Use Jakarta Bean Validation (`@DecimalMin("0.0")`, `@DecimalMax("10.0")`) enforced via `@Valid`.
  * In Python: Use Pydantic field validators / constraints (`Field(ge=0.0, le=10.0)`).

---

## 📹 Submission Deliverable: The 3-Minute Video Defense
To guarantee individual understanding and counteract automated AI generation, your primary evaluation is a **screen recording (max 3 minutes)**:
1. Show your face and speak in English.
2. Open Swagger UI (`/docs` in FastAPI or Thunder Client / Postman in Spring Boot).
3. Send a request that triggers **409 Conflict** and explain which layer caught it and why.
4. Open your codebase and show how you decoupled the Controller from the Repository.
5. **Cold-Call Alert:** Tomorrow at the start of class, 3 students will be randomly selected to project their code and explain their refactoring live.
