package com.college.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {
    private boolean success;
    private String message;
    private String otp; // Only for development/testing, remove in production
}
