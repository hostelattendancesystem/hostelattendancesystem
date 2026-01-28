package com.college.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {
    private String sic;
    private String email;
    private String otp;
    private String newEmail;
}
