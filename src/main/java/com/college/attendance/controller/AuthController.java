package com.college.attendance.controller;

import com.college.attendance.dto.OtpRequest;
import com.college.attendance.dto.OtpResponse;
import com.college.attendance.entity.Student;
import com.college.attendance.repository.StudentRepository;
import com.college.attendance.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {

    private final OtpService otpService;
    private final StudentRepository studentRepository;

    /**
     * Send OTP for signup
     */
    @PostMapping("/send-signup-otp")
    public ResponseEntity<OtpResponse> sendSignupOtp(@RequestBody OtpRequest request) {
        try {
            // Check if SIC or email already exists
            if (studentRepository.findBySic(request.getSic()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new OtpResponse(false, "SIC already registered", null));
            }
            
            if (studentRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new OtpResponse(false, "Email already registered", null));
            }
            
            String otp = otpService.sendSignupOtp(request.getSic(), request.getEmail());
            log.info("Signup OTP sent for SIC: {}", request.getSic());
            
            // Return OTP in response for development/testing (remove in production)
            return ResponseEntity.ok(new OtpResponse(true, "OTP sent successfully", otp));
        } catch (Exception e) {
            log.error("Error sending signup OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new OtpResponse(false, "Failed to send OTP", null));
        }
    }

    /**
     * Send OTP for login
     */
    @PostMapping("/send-login-otp")
    public ResponseEntity<OtpResponse> sendLoginOtp(@RequestBody OtpRequest request) {
        try {
            // Verify student exists
            Optional<Student> studentOpt = studentRepository.findBySic(request.getSic());
            if (studentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new OtpResponse(false, "Student not found", null));
            }
            
            Student student = studentOpt.get();
            String otp = otpService.sendLoginOtp(request.getSic(), student.getEmail());
            log.info("Login OTP sent for SIC: {}", request.getSic());
            
            return ResponseEntity.ok(new OtpResponse(true, "OTP sent successfully", otp));
        } catch (Exception e) {
            log.error("Error sending login OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new OtpResponse(false, "Failed to send OTP", null));
        }
    }

    /**
     * Verify login OTP
     */
    @PostMapping("/verify-login-otp")
    public ResponseEntity<OtpResponse> verifyLoginOtp(@RequestBody OtpRequest request) {
        try {
            boolean isValid = otpService.verifyOtp(request.getSic(), request.getOtp());
            
            if (isValid) {
                log.info("Login OTP verified for SIC: {}", request.getSic());
                return ResponseEntity.ok(new OtpResponse(true, "OTP verified successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new OtpResponse(false, "Invalid or expired OTP", null));
            }
        } catch (Exception e) {
            log.error("Error verifying login OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new OtpResponse(false, "Verification failed", null));
        }
    }

    /**
     * Send OTP for admin login
     */
    @PostMapping("/send-admin-otp")
    public ResponseEntity<OtpResponse> sendAdminOtp(@RequestBody OtpRequest request) {
        try {
            String otp = otpService.sendAdminOtp(request.getEmail());
            log.info("Admin OTP sent to: {}", request.getEmail());
            
            return ResponseEntity.ok(new OtpResponse(true, "Admin OTP sent successfully", otp));
        } catch (Exception e) {
            log.error("Error sending admin OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new OtpResponse(false, "Failed to send OTP", null));
        }
    }

    /**
     * Verify admin OTP
     */
    @PostMapping("/verify-admin-otp")
    public ResponseEntity<OtpResponse> verifyAdminOtp(@RequestBody OtpRequest request) {
        try {
            boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
            
            if (isValid) {
                log.info("Admin OTP verified for: {}", request.getEmail());
                return ResponseEntity.ok(new OtpResponse(true, "Admin verified successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new OtpResponse(false, "Invalid or expired OTP", null));
            }
        } catch (Exception e) {
            log.error("Error verifying admin OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new OtpResponse(false, "Verification failed", null));
        }
    }

    /**
     * Send OTP for email change
     */
    @PostMapping("/send-email-change-otp")
    public ResponseEntity<OtpResponse> sendEmailChangeOtp(@RequestBody OtpRequest request) {
        try {
            // Check if new email already exists
            if (studentRepository.findByEmail(request.getNewEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new OtpResponse(false, "Email already in use", null));
            }
            
            String otp = otpService.sendEmailChangeOtp(request.getSic(), request.getNewEmail());
            log.info("Email change OTP sent for SIC: {} to new email: {}", request.getSic(), request.getNewEmail());
            
            return ResponseEntity.ok(new OtpResponse(true, "OTP sent to new email", otp));
        } catch (Exception e) {
            log.error("Error sending email change OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new OtpResponse(false, "Failed to send OTP", null));
        }
    }

    /**
     * Verify email change OTP
     */
    @PostMapping("/verify-email-change-otp")
    public ResponseEntity<OtpResponse> verifyEmailChangeOtp(@RequestBody OtpRequest request) {
        try {
            boolean isValid = otpService.verifyOtp(request.getNewEmail(), request.getOtp());
            
            if (isValid) {
                log.info("Email change OTP verified for new email: {}", request.getNewEmail());
                return ResponseEntity.ok(new OtpResponse(true, "Email verified successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new OtpResponse(false, "Invalid or expired OTP", null));
            }
        } catch (Exception e) {
            log.error("Error verifying email change OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new OtpResponse(false, "Verification failed", null));
        }
    }
}
