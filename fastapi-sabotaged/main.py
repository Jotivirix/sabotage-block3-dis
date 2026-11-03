# ⚠️ FLAGGED ARCHITECTURAL SMELL:
# 1. No layered architecture (Service and Repository missing; everything dumped in main.py).
# 2. Broken HTTP semantics (200 OK used everywhere).
# 3. Incomplete validation and raw JSON reads directly in route handler.

from fastapi import FastAPI, Response, status
from pydantic import BaseModel
from typing import Optional, List
import json
import os

app = FastAPI(title="Sabotaged Enrollment API")

DATA_FILE = "enrollments_raw.json"

def read_db():
    if not os.path.exists(DATA_FILE):
        return []
    with open(DATA_FILE, "r") as f:
        return json.load(f)

def write_db(data):
    with open(DATA_FILE, "w") as f:
        json.dump(data, f, indent=2)

# BAD SMELL: Single schema with no separation between create and persisted entity
class EnrollmentRaw(BaseModel):
    id: Optional[int] = None
    student_id: int
    subject_id: int
    grade: Optional[float] = None  # BAD SMELL: Missing Field(ge=0, le=10)

@app.post("/api/v1/enrollments")
def create_enrollment_bad(enrollment: EnrollmentRaw, response: Response):
    records = read_db() # BAD SMELL: Direct I/O in router!
    
    # BAD SMELL: Manual validation logic
    if enrollment.grade is not None and (enrollment.grade < 0 or enrollment.grade > 10):
        response.status_code = status.HTTP_200_OK # BAD SMELL: HTTP 200 for validation error!
        return {"error": "Grade must be between 0 and 10"}
    
    # BAD SMELL: Business rules in router
    for r in records:
        if r["student_id"] == enrollment.student_id and r["subject_id"] == enrollment.subject_id:
            response.status_code = status.HTTP_200_OK # BAD SMELL: Should be 409 Conflict!
            return {"error": "Student already enrolled in this subject"}
            
    new_data = enrollment.dict()
    new_data["id"] = len(records) + 1
    records.append(new_data)
    write_db(records)
    
    # BAD SMELL: Returns 200 OK instead of 201 Created
    return new_data
