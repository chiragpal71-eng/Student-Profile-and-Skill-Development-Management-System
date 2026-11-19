package com.example.studentmanagement.controller;

import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // 1. Add a student
    @PostMapping
    public ResponseEntity addStudent(@Valid @RequestBody Student student) {
        try {
            Student savedStudent = studentService.addStudent(student);
            return new ResponseEntity(savedStudent, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. Retrieve all students
    @GetMapping
    public ResponseEntity getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    // 3. Search for a student by ID
    @GetMapping("/{id}")
    public ResponseEntity getStudentById(@PathVariable("id") String studentId) {
        Student student = studentService.searchById(studentId);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Student not found with ID: " + studentId));
    }

    // 4. Update student scores and skills
    @PutMapping("/{id}")
    public ResponseEntity updateScoresAndSkills(
            @PathVariable("id") String studentId,
            @RequestBody StudentUpdateRequest updateRequest) {
        try {
            Student updated = studentService.updateScoresAndSkills(
                    studentId,
                    updateRequest.getProgrammingScore(),
                    updateRequest.getMathematicsScore(),
                    updateRequest.getTechnicalSkills()
            );
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 5. Delete a student record
    @DeleteMapping("/{id}")
    public ResponseEntity deleteStudent(@PathVariable("id") String studentId) {
        try {
            studentService.deleteStudent(studentId);
            return ResponseEntity.ok(Map.of("message", "Student deleted successfully: " + studentId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // 6. Display the merit list
    @GetMapping("/merit-list")
    public ResponseEntity getMeritList() {
        return ResponseEntity.ok(studentService.generateMeritList());
    }

    // Extra endpoints for Q2 analytics
    @GetMapping("/search/email")
    public ResponseEntity getStudentByEmail(@RequestParam String email) {
        Student student = studentService.searchByEmail(email);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Student not found with email: " + email));
    }

    @GetMapping("/search/course")
    public ResponseEntity getStudentsByCourse(@RequestParam String course) {
        return ResponseEntity.ok(studentService.searchByCourse(course));
    }

    @GetMapping("/shared-skills")
    public ResponseEntity getStudentsWithSharedSkills() {
        return ResponseEntity.ok(studentService.groupStudentsBySkill());
    }

    @GetMapping("/weak-programming-strong-math")
    public ResponseEntity getWeakProgrammingStrongMath() {
        return ResponseEntity.ok(studentService.findWeakProgrammingStrongMathStudents());
    }

    // DTO for Update Request
    public static class StudentUpdateRequest {
        private double programmingScore;
        private double mathematicsScore;
        private String[] technicalSkills;

        public double getProgrammingScore() {
            return programmingScore;
        }

        public void setProgrammingScore(double programmingScore) {
            this.programmingScore = programmingScore;
        }

        public double getMathematicsScore() {
            return mathematicsScore;
        }

        public void setMathematicsScore(double mathematicsScore) {
            this.mathematicsScore = mathematicsScore;
        }

        public String[] getTechnicalSkills() {
            return technicalSkills;
        }

        public void setTechnicalSkills(String[] technicalSkills) {
            this.technicalSkills = technicalSkills;
        }
    }
}