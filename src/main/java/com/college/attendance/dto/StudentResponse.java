package com.college.attendance.dto;

import com.college.attendance.entity.Student.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long slNo;
    private String sic;
    private String email;
    private LocalDateTime addedOn;
    private Integer attendanceCount;
    private Boolean isTaken;
    private StudentStatus status;
    private LocalDate pauseTill;
    private Boolean isVerified;
    private LocalDateTime takenOn;
}
