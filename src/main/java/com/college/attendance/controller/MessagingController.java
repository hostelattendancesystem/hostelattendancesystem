package com.college.attendance.controller;

import com.college.attendance.dto.MessageRequest;
import com.college.attendance.dto.MessageResponse;
import com.college.attendance.entity.Student;
import com.college.attendance.repository.StudentRepository;
import com.college.attendance.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessagingController {

    private final EmailService emailService;
    private final StudentRepository studentRepository;

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest request) {
        log.info("Sending message to: {}", request.getToEmail());
        
        try {
            if (request.getSendToAll() != null && request.getSendToAll()) {
                // Send to all students
                return sendMessageToAll(request);
            } else {
                // Send to single student
                emailService.sendCustomMessage(
                    request.getToEmail(),
                    request.getSubject(),
                    request.getBody()
                );
                
                log.info("Message sent successfully to: {}", request.getToEmail());
                return ResponseEntity.ok(new MessageResponse(
                    true, 
                    "Message sent successfully", 
                    1
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send message", e);
            return ResponseEntity.badRequest().body(new MessageResponse(
                false, 
                "Failed to send message: " + e.getMessage(), 
                0
            ));
        }
    }

    @PostMapping("/send-all")
    public ResponseEntity<MessageResponse> sendMessageToAll(@RequestBody MessageRequest request) {
        log.info("Sending message to all students");
        
        try {
            List<Student> students = studentRepository.findAll();
            int successCount = 0;
            
            for (Student student : students) {
                try {
                    emailService.sendCustomMessage(
                        student.getEmail(),
                        request.getSubject(),
                        request.getBody()
                    );
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to send message to: {}", student.getEmail(), e);
                }
            }
            
            log.info("Message sent to {} out of {} students", successCount, students.size());
            return ResponseEntity.ok(new MessageResponse(
                true, 
                "Message sent to " + successCount + " students", 
                successCount
            ));
        } catch (Exception e) {
            log.error("Failed to send messages to all students", e);
            return ResponseEntity.badRequest().body(new MessageResponse(
                false, 
                "Failed to send messages: " + e.getMessage(), 
                0
            ));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Messaging controller is working!");
    }
}
