package com.college.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sl_no")
    private Long slNo;

    @Column(name = "sic", nullable = false, unique = true, length = 50)
    private String sic;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "added_on", nullable = false)
    private LocalDateTime addedOn;

    @Column(name = "attendance_count", nullable = false)
    private Integer attendanceCount = 0;

    @Column(name = "is_taken", nullable = false)
    private Boolean isTaken = false;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "pause_till")
    private LocalDate pauseTill;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "taken_on")
    private LocalDateTime takenOn;

    @PrePersist
    protected void onCreate() {
        if (addedOn == null) {
            addedOn = LocalDateTime.now();
        }
        if (attendanceCount == null) {
            attendanceCount = 0;
        }
        if (isTaken == null) {
            isTaken = false;
        }
        if (status == null) {
            status = StudentStatus.ACTIVE;
        }
        if (isVerified == null) {
            isVerified = false;
        }
    }

    public enum StudentStatus {
        ACTIVE,
        PAUSED,
        DEACTIVATED
    }
}
