package com.ufv.dsi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

/**
 * ⚠️ FLAGGED ARCHITECTURAL SMELL:
 * 1. Direct file access & repository bypass inside Controller.
 * 2. Business logic (duplicate checks) implemented in Controller.
 * 3. 200 OK returned on creation and error payloads returned with HTTP 200!
 */
@RestController
@RequestMapping("/api/v1/enrollments")
public class SabotagedEnrollmentController {

    // BAD SMELL: Field injection + Controller talking to storage layer
    @Autowired
    private SabotagedRepository repository;

    @PostMapping
    public ResponseEntity<?> createEnrollment(@RequestBody Map<String, Object> payload) {
        Integer studentId = (Integer) payload.get("studentId");
        Integer subjectId = (Integer) payload.get("subjectId");
        Double grade = payload.get("grade") != null ? Double.valueOf(payload.get("grade").toString()) : null;

        // BAD SMELL: Manual validation instead of @Valid
        if (grade != null && (grade < 0.0 || grade > 10.0)) {
            // BAD SMELL: 200 OK with error body!
            return ResponseEntity.ok(Map.of("status", "error", "message", "Invalid grade range"));
        }

        // BAD SMELL: Business logic directly in controller
        List<Map<String, Object>> all = repository.loadRawData();
        for (Map<String, Object> row : all) {
            if (row.get("studentId").equals(studentId) && row.get("subjectId").equals(subjectId)) {
                // BAD SMELL: Should be 409 Conflict!
                return ResponseEntity.status(200).body("Error: Already enrolled");
            }
        }

        Map<String, Object> newRecord = new HashMap<>(payload);
        newRecord.put("id", (int)(Math.random() * 10000));
        repository.saveRawData(newRecord);

        // BAD SMELL: 200 OK instead of 201 Created with Location header!
        return ResponseEntity.ok(newRecord);
    }
}
