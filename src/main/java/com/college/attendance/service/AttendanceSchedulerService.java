package com.college.attendance.service;

import com.college.attendance.entity.Student;
import com.college.attendance.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceSchedulerService {

    private final StudentRepository studentRepository;
    private final AttendanceApiService attendanceApiService;
    private final EmailService emailService;
    private final StudentService studentService;

    @Value("${app.attendance.start-time}")
    private String startTime;

    @Value("${app.attendance.end-time}")
    private String endTime;

    @Value("${app.attendance.verification-delay-min}")
    private int verificationDelayMin;

    @Value("${app.attendance.verification-delay-max}")
    private int verificationDelayMax;

    private final Random random = new Random();

    // Run every minute to check if it's time to mark attendance
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndMarkAttendance() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        if (now.isAfter(start) && now.isBefore(end)) {
            log.info("Current time {} is within attendance window ({} - {})", now, start, end);
            markAttendanceForEligibleStudents();
        }
    }

    @Transactional
    public void markAttendanceForEligibleStudents() {
        LocalDate today = LocalDate.now();
        List<Student> students = studentRepository.findStudentsForAttendance(today);

        log.info("Found {} students eligible for attendance", students.size());

        for (Student student : students) {
            try {
                boolean success = attendanceApiService.markAttendance(student.getSic());

                if (success) {
                    student.setIsTaken(true);
                    student.setTakenOn(LocalDateTime.now()); // Set the timestamp when attendance is taken
                    student.setAttendanceCount(student.getAttendanceCount() + 1);
                    studentRepository.save(student);

                    emailService.sendAttendanceConfirmation(student.getEmail(), student.getSic(), false);
                    log.info("Attendance marked for SIC: {} at {}", student.getSic(), student.getTakenOn());

                    // Schedule verification with random delay
                    scheduleVerification(student);
                } else {
                    emailService.sendAttendanceFailure(student.getEmail(), student.getSic(), false);
                    log.error("Failed to mark attendance for SIC: {}", student.getSic());
                }
            } catch (Exception e) {
                log.error("Error processing attendance for SIC: {}", student.getSic(), e);
            }
        }
    }

    private void scheduleVerification(Student student) {
        int delayMinutes = verificationDelayMin + random.nextInt(verificationDelayMax - verificationDelayMin + 1);
        log.info("Scheduling verification for SIC: {} after {} minutes", student.getSic(), delayMinutes);

        new Thread(() -> {
            try {
                TimeUnit.MINUTES.sleep(delayMinutes);
                verifyAttendance(student.getSic());
            } catch (InterruptedException e) {
                log.error("Verification thread interrupted for SIC: {}", student.getSic(), e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Transactional
    public void verifyAttendance(String sic) {
        Student student = studentRepository.findBySic(sic).orElse(null);
        
        if (student == null) {
            log.warn("Student not found for verification: {}", sic);
            return;
        }

        if (!student.getIsTaken() || student.getIsVerified()) {
            log.info("Skipping verification for SIC: {} (already verified or not taken)", sic);
            return;
        }

        try {
            boolean success = attendanceApiService.verifyAttendance(student.getSic());

            if (success) {
                student.setIsVerified(true);
                studentRepository.save(student);
                emailService.sendAttendanceConfirmation(student.getEmail(), student.getSic(), true);
                log.info("Attendance verified for SIC: {}", student.getSic());
            } else {
                emailService.sendAttendanceFailure(student.getEmail(), student.getSic(), true);
                log.error("Failed to verify attendance for SIC: {}", student.getSic());
            }
        } catch (Exception e) {
            log.error("Error verifying attendance for SIC: {}", student.getSic(), e);
        }
    }

    // Reset daily flags at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetDailyFlags() {
        log.info("Running daily reset at midnight");
        studentService.resetDailyFlags();
        studentService.updateExpiredPauses();
    }

    // Also reset at the start of attendance window as a backup
    @Scheduled(cron = "0 30 19 * * *") // 7:30 PM (5 minutes before attendance window)
    @Transactional
    public void resetBeforeAttendance() {
        log.info("Running pre-attendance reset and pause update");
        studentService.resetDailyFlags(); // Reset flags if date changed (backup if midnight reset failed)
        studentService.updateExpiredPauses();
    }
}
