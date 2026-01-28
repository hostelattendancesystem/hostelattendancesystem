package com.college.attendance.service;

import com.college.attendance.dto.StudentRequest;
import com.college.attendance.dto.StudentResponse;
import com.college.attendance.entity.Student;
import com.college.attendance.entity.Student.StudentStatus;
import com.college.attendance.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final EmailService emailService;

    @Transactional
    public StudentResponse registerStudent(StudentRequest request) {
        log.info("Registering student with SIC: {}", request.getSic());

        // Check if student already exists
        if (studentRepository.existsBySic(request.getSic())) {
            throw new RuntimeException("Student with SIC " + request.getSic() + " already exists");
        }

        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Student with email " + request.getEmail() + " already exists");
        }

        Student student = new Student();
        student.setSic(request.getSic());
        student.setEmail(request.getEmail());
        student.setAddedOn(LocalDateTime.now());
        student.setAttendanceCount(0);
        student.setIsTaken(false);
        student.setStatus(request.getStatus() != null ? request.getStatus() : StudentStatus.ACTIVE);
        student.setPauseTill(request.getPauseTill());
        student.setIsVerified(false);

        Student savedStudent = studentRepository.save(student);
        log.info("Student registered successfully: {}", savedStudent.getSic());

        return mapToResponse(savedStudent);
    }

    @Transactional
    public StudentResponse updateStudent(String sic, StudentRequest request) {
        log.info("Updating student with SIC: {}", sic);

        Student student = studentRepository.findBySic(sic)
                .orElseThrow(() -> new RuntimeException("Student not found with SIC: " + sic));

        if (request.getEmail() != null && !request.getEmail().equals(student.getEmail())) {
            if (studentRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            String oldEmail = student.getEmail();
            student.setEmail(request.getEmail());
            
            // Send email notification about email change
            emailService.sendChangeNotification(
                request.getEmail(), 
                student.getSic(), 
                "Email Changed", 
                "Your email has been changed from " + oldEmail + " to " + request.getEmail()
            );
        }

        // Enhanced pause and status logic
        if (request.getStatus() != null) {
            StudentStatus oldStatus = student.getStatus();
            StudentStatus newStatus = request.getStatus();
            
            // If changing to ACTIVE or DEACTIVATED, clear pauseTill date
            if ((newStatus == StudentStatus.ACTIVE || newStatus == StudentStatus.DEACTIVATED) 
                && oldStatus != newStatus) {
                student.setPauseTill(null);
                log.info("Status changed to {} for SIC: {}, clearing pauseTill", newStatus, sic);
            }
            
            student.setStatus(newStatus);
            
            // Send notification about status change
            if (oldStatus != newStatus) {
                emailService.sendChangeNotification(
                    student.getEmail(),
                    student.getSic(),
                    "Account Status Changed",
                    "Your account status has been changed from " + oldStatus + " to " + newStatus
                );
            }
        }

        // If pauseTill is set AND status is not ACTIVE or DEACTIVATED, set pause date
        if (request.getPauseTill() != null) {
            // Only set pauseTill if status is PAUSED (or will be PAUSED)
            if (student.getStatus() == StudentStatus.PAUSED) {
                student.setPauseTill(request.getPauseTill());
                log.info("PauseTill date set for SIC: {}, status is PAUSED", sic);
                
                // Send notification about pause date
                emailService.sendChangeNotification(
                    student.getEmail(),
                    student.getSic(),
                    "Pause Date Set",
                    "Your account has been paused until " + request.getPauseTill()
                );
            } else if (student.getStatus() != StudentStatus.ACTIVE && student.getStatus() != StudentStatus.DEACTIVATED) {
                // If status is not explicitly set, set it to PAUSED
                student.setPauseTill(request.getPauseTill());
                student.setStatus(StudentStatus.PAUSED);
                log.info("PauseTill date set for SIC: {}, status changed to PAUSED", sic);
                
                // Send notification about pause date
                emailService.sendChangeNotification(
                    student.getEmail(),
                    student.getSic(),
                    "Pause Date Set",
                    "Your account has been paused until " + request.getPauseTill()
                );
            }
            // If status is ACTIVE or DEACTIVATED, ignore the pauseTill from request
        }
        
        // Allow admin to update attendance count
        if (request.getAttendanceCount() != null) {
            Integer oldCount = student.getAttendanceCount();
            student.setAttendanceCount(request.getAttendanceCount());
            
            if (!oldCount.equals(request.getAttendanceCount())) {
                emailService.sendChangeNotification(
                    student.getEmail(),
                    student.getSic(),
                    "Attendance Count Updated",
                    "Your attendance count has been updated from " + oldCount + " to " + request.getAttendanceCount()
                );
            }
        }
        
        // Allow admin to update isTaken flag
        if (request.getIsTaken() != null) {
            student.setIsTaken(request.getIsTaken());
        }
        
        // Allow admin to update isVerified flag
        if (request.getIsVerified() != null) {
            student.setIsVerified(request.getIsVerified());
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("Student updated successfully: {}", updatedStudent.getSic());

        return mapToResponse(updatedStudent);
    }

    public StudentResponse getStudent(String sic) {
        Student student = studentRepository.findBySic(sic)
                .orElseThrow(() -> new RuntimeException("Student not found with SIC: " + sic));
        return mapToResponse(student);
    }

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteStudent(String sic) {
        Student student = studentRepository.findBySic(sic)
                .orElseThrow(() -> new RuntimeException("Student not found with SIC: " + sic));
        studentRepository.delete(student);
        log.info("Student deleted: {}", sic);
    }

    @Transactional
    public void resetDailyFlags() {
        log.info("Resetting daily flags for all students");
        LocalDate today = LocalDate.now();
        List<Student> students = studentRepository.findAll();
        
        int resetCount = 0;
        for (Student student : students) {
            // Check if takenOn exists and is not today's date
            if (student.getTakenOn() != null) {
                LocalDate takenDate = student.getTakenOn().toLocalDate();
                
                // If attendance was taken on a different day, reset the flags
                if (!takenDate.isEqual(today)) {
                    student.setIsTaken(false);
                    student.setIsVerified(false);
                    resetCount++;
                    log.debug("Resetting flags for SIC: {} (last taken: {})", student.getSic(), takenDate);
                }
            } else {
                // If takenOn is null but flags are set, reset them
                if (student.getIsTaken() || student.getIsVerified()) {
                    student.setIsTaken(false);
                    student.setIsVerified(false);
                    resetCount++;
                    log.debug("Resetting flags for SIC: {} (takenOn is null)", student.getSic());
                }
            }
        }
        
        studentRepository.saveAll(students);
        log.info("Daily flags reset for {} out of {} students", resetCount, students.size());
    }

    @Transactional
    public void updateExpiredPauses() {
        log.info("Updating students with expired pause dates");
        LocalDate today = LocalDate.now();
        List<Student> students = studentRepository.findStudentsWithExpiredPause(today);
        
        students.forEach(student -> {
            student.setStatus(StudentStatus.ACTIVE);
            student.setPauseTill(null);
        });
        
        studentRepository.saveAll(students);
        log.info("Updated {} students from PAUSED to ACTIVE", students.size());
    }

    private StudentResponse mapToResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setSlNo(student.getSlNo());
        response.setSic(student.getSic());
        response.setEmail(student.getEmail());
        response.setAddedOn(student.getAddedOn());
        response.setAttendanceCount(student.getAttendanceCount());
        response.setIsTaken(student.getIsTaken());
        response.setStatus(student.getStatus());
        response.setPauseTill(student.getPauseTill());
        response.setIsVerified(student.getIsVerified());
        response.setTakenOn(student.getTakenOn());
        return response;
    }
}
