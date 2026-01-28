package com.college.attendance.dto;

import com.college.attendance.entity.Student.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequest {

    @NotBlank(message = "SIC is required")
    private String sic;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private StudentStatus status;

    private LocalDate pauseTill;
    
    private Integer attendanceCount;
    
    private Boolean isTaken;
    
    private Boolean isVerified;
}
