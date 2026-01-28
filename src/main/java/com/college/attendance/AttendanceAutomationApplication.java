package com.college.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AttendanceAutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceAutomationApplication.class, args);
    }
}
