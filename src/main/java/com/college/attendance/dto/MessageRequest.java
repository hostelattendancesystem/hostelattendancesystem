package com.college.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    
    private String toEmail;
    private String subject;
    private String body;
    private Boolean sendToAll;
}
