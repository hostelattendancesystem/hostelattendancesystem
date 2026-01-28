package com.college.attendance.controller;

import com.college.attendance.dto.StudentRequest;
import com.college.attendance.dto.StudentResponse;
import com.college.attendance.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/register")
    public ResponseEntity<StudentResponse> registerStudent(@Valid @RequestBody StudentRequest request) {
        try {
            StudentResponse response = studentService.registerStudent(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{sic}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable String sic) {
        try {
            StudentResponse response = studentService.getStudent(sic);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        List<StudentResponse> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @PutMapping("/{sic}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable String sic,
            @Valid @RequestBody StudentRequest request) {
        try {
            StudentResponse response = studentService.updateStudent(sic, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{sic}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String sic) {
        try {
            studentService.deleteStudent(sic);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
