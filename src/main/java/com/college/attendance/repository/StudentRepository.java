package com.college.attendance.repository;

import com.college.attendance.entity.Student;
import com.college.attendance.entity.Student.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findBySic(String sic);

    Optional<Student> findByEmail(String email);

    boolean existsBySic(String sic);

    boolean existsByEmail(String email);

    // Find students eligible for attendance (ACTIVE or PAUSED with expired pause date)
    @Query("SELECT s FROM Student s WHERE s.status = 'ACTIVE' OR " +
           "(s.status = 'PAUSED' AND s.pauseTill <= :today)")
    List<Student> findEligibleStudents(LocalDate today);

    // Find students who haven't taken attendance today
    @Query("SELECT s FROM Student s WHERE s.isTaken = false AND " +
           "(s.status = 'ACTIVE' OR (s.status = 'PAUSED' AND s.pauseTill <= :today))")
    List<Student> findStudentsForAttendance(LocalDate today);

    // Find students who need verification
    @Query("SELECT s FROM Student s WHERE s.isTaken = true AND s.isVerified = false AND " +
           "(s.status = 'ACTIVE' OR (s.status = 'PAUSED' AND s.pauseTill <= :today))")
    List<Student> findStudentsForVerification(LocalDate today);

    // Find students with expired pause
    @Query("SELECT s FROM Student s WHERE s.status = 'PAUSED' AND s.pauseTill <= :today")
    List<Student> findStudentsWithExpiredPause(LocalDate today);
}
