package com.college.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceApiService {

    @Value("${app.attendance.url}")
    private String attendanceUrl;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public boolean markAttendance(String sic) {
        String url = attendanceUrl + sic;
        log.info("Marking attendance for SIC: {} at URL: {}", sic, url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            boolean success = response.isSuccessful();
            
            if (success) {
                log.info("Attendance marked successfully for SIC: {}", sic);
                String responseBody = response.body() != null ? response.body().string() : "";
                log.debug("Response: {}", responseBody);
            } else {
                log.error("Failed to mark attendance for SIC: {}. Status code: {}", sic, response.code());
            }
            
            return success;
        } catch (IOException e) {
            log.error("Error marking attendance for SIC: {}", sic, e);
            return false;
        }
    }

    public boolean verifyAttendance(String sic) {
        log.info("Verifying attendance for SIC: {}", sic);
        return markAttendance(sic); // Same API call for verification
    }
}
