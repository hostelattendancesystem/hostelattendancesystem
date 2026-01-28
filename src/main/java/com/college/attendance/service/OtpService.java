package com.college.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    // Store OTPs with expiration time (email/sic -> OTP data)
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    
    /**
     * Get disclaimer text for OTP emails (HTML formatted)
     */
    private String getDisclaimerHtml() {
        return "<div style='background: rgba(239, 68, 68, 0.1); border: 2px solid rgba(239, 68, 68, 0.3); border-radius: 12px; padding: 20px; margin: 24px 0;'>" +
               "  <div style='text-align: center; margin-bottom: 16px;'>" +
               "    <div style='font-size: 32px; margin-bottom: 8px;'>⚠</div>" +
               "    <h2 style='margin: 0; color: #ef4444; font-size: 18px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px;'>IMPORTANT LEGAL NOTICE</h2>" +
               "  </div>" +
               "  <div style='background: rgba(0, 0, 0, 0.2); border-radius: 8px; padding: 16px;'>" +
               "    <p style='margin: 0 0 12px; color: rgba(255, 255, 255, 0.95); font-size: 14px; line-height: 1.6;'><strong style='color: #fbbf24;'>This is an ACADEMIC PROJECT for educational purposes only.</strong></p>" +
               "    <p style='margin: 0 0 12px; color: rgba(255, 255, 255, 0.9); font-size: 13px; line-height: 1.6;'>By using this system, you acknowledge and agree that:</p>" +
               "    <ul style='margin: 0; padding-left: 20px; color: rgba(255, 255, 255, 0.85); font-size: 12px; line-height: 1.8;'>" +
               "      <li>This is a <strong>STUDENT PROJECT</strong> for testing and evaluation only</li>" +
               "      <li>You will <strong>NOT</strong> use this for actual daily attendance management</li>" +
               "      <li>You are <strong>SOLELY RESPONSIBLE</strong> for any consequences</li>" +
               "      <li>The developer is <strong>NOT LIABLE</strong> for any issues or problems</li>" +
               "      <li>The developer has the <strong>RIGHT TO SHARE</strong> your data with authorities if any problems arise from college or government</li>" +
               "      <li>YOU are the <strong>SOLE CASE HOLDER</strong> for any legal/disciplinary actions</li>" +
               "      <li>You must <strong>INDEMNIFY AND HOLD HARMLESS</strong> the developer</li>" +
               "      <li>These terms are <strong>BINDING</strong> upon registration</li>" +
               "    </ul>" +
               "    <p style='margin: 12px 0 0; color: rgba(255, 255, 255, 0.75); font-size: 11px; font-style: italic;'>For complete terms, visit the registration page.</p>" +
               "  </div>" +
               "</div>";
    }

    /**
     * Generate a 6-digit OTP
     */
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Send signup OTP
     */
    public String sendSignupOtp(String sic, String email) {
        String otp = generateOtp();
        storeOtp(email, otp);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Verify Your Email - Attendance Automation");
            
            String htmlContent = buildOtpEmailTemplate(
                "Dear Student,",
                "Verify Your Email",
                "Welcome to the Attendance Automation System! Please verify your email address using the OTP code below.",
                otp,
                sic,
                "If you didn't request this, please ignore this email. Your account will not be created without verification."
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Signup OTP HTML email sent to {} for SIC: {}", email, sic);
            return otp; // Return for development/testing
        } catch (MessagingException e) {
            log.error("Failed to send signup OTP HTML email to {}", email, e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    /**
     * Send login OTP
     */
    public String sendLoginOtp(String sic, String email) {
        String otp = generateOtp();
        storeOtp(sic, otp);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Login OTP - Attendance Automation");
            
            String htmlContent = buildOtpEmailTemplate(
                "Dear Student,",
                "Login OTP",
                "Your login OTP is ready. Please use this code to access your account.",
                otp,
                sic,
                "If you didn't request this, please secure your account immediately. Someone may be trying to access your account."
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Login OTP HTML email sent to {} for SIC: {}", email, sic);
            return otp;
        } catch (MessagingException e) {
            log.error("Failed to send login OTP HTML email to {}", email, e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    /**
     * Send admin OTP
     */
    public String sendAdminOtp(String email) {
        String otp = generateOtp();
        storeOtp(email, otp);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Admin Verification OTP - Attendance Automation");
            
            String htmlContent = buildOtpEmailTemplate(
                "Dear Administrator,",
                "Admin Verification OTP",
                "Your admin verification OTP is ready. Please use this code to access the admin panel.",
                otp,
                null, // No SIC for admin
                "If you didn't request this, please secure your admin account immediately. This OTP is for administrative access only."
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Admin OTP HTML email sent to {}", email);
            return otp;
        } catch (MessagingException e) {
            log.error("Failed to send admin OTP HTML email to {}", email, e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    /**
     * Send email change OTP
     */
    public String sendEmailChangeOtp(String sic, String newEmail) {
        String otp = generateOtp();
        storeOtp(newEmail, otp);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(newEmail);
            helper.setSubject("Verify New Email - Attendance Automation");
            
            String htmlContent = buildOtpEmailTemplate(
                "Dear Student,",
                "Verify New Email",
                "You have requested to change your email address. Please verify your new email using the OTP code below.",
                otp,
                sic,
                "If you didn't request this change, please contact support immediately. Your email will not be changed without verification."
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email change OTP HTML email sent to {} for SIC: {}", newEmail, sic);
            return otp;
        } catch (MessagingException e) {
            log.error("Failed to send email change OTP HTML email to {}", newEmail, e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    /**
     * Verify OTP
     */
    public boolean verifyOtp(String key, String otp) {
        OtpData otpData = otpStore.get(key);
        
        if (otpData == null) {
            log.warn("No OTP found for key: {}", key);
            return false;
        }
        
        if (otpData.isExpired()) {
            log.warn("OTP expired for key: {}", key);
            otpStore.remove(key);
            return false;
        }
        
        if (otpData.getOtp().equals(otp)) {
            otpStore.remove(key); // Remove OTP after successful verification
            log.info("OTP verified successfully for key: {}", key);
            return true;
        }
        
        log.warn("Invalid OTP for key: {}", key);
        return false;
    }

    /**
     * Store OTP with expiration
     */
    private void storeOtp(String key, String otp) {
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        otpStore.put(key, new OtpData(otp, expiryTime));
        log.debug("OTP stored for key: {} with expiry: {}", key, expiryTime);
    }

    /**
     * Clean up expired OTPs (can be called periodically)
     */
    public void cleanupExpiredOtps() {
        otpStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.debug("Cleaned up expired OTPs. Remaining: {}", otpStore.size());
    }
    
    /**
     * Build HTML email template for OTP emails
     */
    private String buildOtpEmailTemplate(String greeting, String title, String message, String otp, String sic, String additionalInfo) {
        int currentYear = LocalDateTime.now().getYear();
        
        String otpDisplay = 
            "<div style='text-align: center; margin: 32px 0;'>" +
            "  <div style='background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%); border: 2px solid rgba(102, 126, 234, 0.4); border-radius: 16px; padding: 24px; display: inline-block;'>" +
            "    <div style='font-size: 12px; color: rgba(255, 255, 255, 0.6); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; font-weight: 600;'>Your OTP Code</div>" +
            "    <div onclick=\"navigator.clipboard.writeText('" + otp + "').then(() => alert('OTP copied to clipboard!')).catch(() => {})\" style='font-size: 42px; font-weight: 800; color: #fff; letter-spacing: 8px; font-family: monospace; cursor: pointer; user-select: all; -webkit-user-select: all; -moz-user-select: all; -ms-user-select: all; padding: 8px; border-radius: 8px; transition: background 0.3s ease;' onmouseover=\"this.style.background='rgba(102, 126, 234, 0.3)'\" onmouseout=\"this.style.background='transparent'\" title='Click to copy'>" + otp + "</div>" +
            "    <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); margin-top: 8px;'>Valid for " + OTP_EXPIRY_MINUTES + " minutes • Click to copy</div>" +
            "  </div>" +
            "</div>";
        
        String sicDisplay = sic != null ? 
            "<div style='display: flex; align-items: center; margin-bottom: 12px;'>" +
            "  <span style='font-size: 24px; margin-right: 12px;'>●</span>" +
            "  <div>" +
            "    <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Student ID (SIC)</div>" +
            "    <div style='font-size: 16px; font-weight: 600; color: rgba(255, 255, 255, 0.95);'>" + sic + "</div>" +
            "  </div>" +
            "</div>" : "";
        
        String timeDisplay =
            "<div style='display: flex; align-items: center;'>" +
            "  <span style='font-size: 24px; margin-right: 12px;'>⏱</span>" +
            "  <div>" +
            "    <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Sent At</div>" +
            "    <div style='font-size: 16px; font-weight: 600; color: rgba(255, 255, 255, 0.95);'>" + LocalDateTime.now().format(DATE_FORMATTER) + "</div>" +
            "  </div>" +
            "</div>";
        
        String details = "<div style='margin-bottom: 16px;'>" + sicDisplay + timeDisplay + "</div>";
        
        String content = 
            "<p style='margin: 0 0 16px; color: rgba(255, 255, 255, 0.95); font-size: 16px; line-height: 1.6;'>" + message + "</p>" +
            otpDisplay +
            getDisclaimerHtml();
        
        return "<!DOCTYPE html>" +
            "<html lang='en'>" +
            "<head>" +
            "  <meta charset='UTF-8'>" +
            "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "  <title>" + title + "</title>" +
            "</head>" +
            "<body style='margin: 0; padding: 0; font-family: Inter, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif; background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%); min-height: 100vh;'>" +
            "  <table role='presentation' cellspacing='0' cellpadding='0' border='0' width='100%' style='background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%); padding: 40px 20px;'>" +
            "    <tr>" +
            "      <td align='center'>" +
            "        <table role='presentation' cellspacing='0' cellpadding='0' border='0' width='600' style='max-width: 600px; background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 24px; overflow: hidden; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);'>" +
            "          <tr>" +
            "            <td style='background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%); padding: 40px 40px 30px; text-align: center; border-bottom: 1px solid rgba(255, 255, 255, 0.1);'>" +
            "              <div style='font-size: 64px; margin-bottom: 16px;'>•</div>" +
            "              <h1 style='margin: 0 0 8px; font-size: 28px; font-weight: 800; color: #fff;'>Attendance Automation</h1>" +
            "              <p style='margin: 0; color: rgba(255, 255, 255, 0.8); font-size: 14px; font-weight: 400;'>College Hostel Attendance System</p>" +
            "            </td>" +
            "          </tr>" +
            "          <tr>" +
            "            <td style='padding: 40px;'>" +
            "              <p style='margin: 0 0 24px; color: rgba(255, 255, 255, 0.95); font-size: 16px; line-height: 1.6;'>" + greeting + "</p>" +
            "              <div style='margin-bottom: 32px;'>" + content + "</div>" +
            "              <table role='presentation' cellspacing='0' cellpadding='0' border='0' width='100%' style='background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; margin-bottom: 32px;'>" +
            "                <tr><td style='padding: 24px;'>" + details + "</td></tr>" +
            "              </table>" +
            "              <p style='margin: 0 0 24px; color: rgba(255, 255, 255, 0.75); font-size: 15px; line-height: 1.6;'>" + additionalInfo + "</p>" +
            "              <div style='margin-top: 40px; padding-top: 24px; border-top: 1px solid rgba(255, 255, 255, 0.1);'>" +
            "                <p style='margin: 0 0 8px; color: rgba(255, 255, 255, 0.95); font-size: 15px; font-weight: 600;'>Best regards,</p>" +
            "                <p style='margin: 0; color: rgba(255, 255, 255, 0.75); font-size: 15px;'>Attendance Automation System</p>" +
            "                <p style='margin: 4px 0 0; color: rgba(255, 255, 255, 0.5); font-size: 13px; font-style: italic;'>Academic Project</p>" +
            "              </div>" +
            "            </td>" +
            "          </tr>" +
            "          <tr>" +
            "            <td style='background: rgba(0, 0, 0, 0.2); padding: 24px 40px; text-align: center; border-top: 1px solid rgba(255, 255, 255, 0.1);'>" +
            "              <table role='presentation' cellspacing='0' cellpadding='0' border='0' width='100%' style='margin-bottom: 20px;'>" +
            "                <tr>" +
            "                  <td align='center'>" +
            "                    <table role='presentation' cellspacing='0' cellpadding='0' border='0'>" +
            "                      <tr>" +
            "                        <td style='padding: 8px 12px; background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 50px; margin: 0 4px;'>" +
            "                          <span style='color: rgba(255, 255, 255, 0.9); font-size: 12px;'>Auto Mark Daily</span>" +
            "                        </td>" +
            "                        <td style='padding: 8px 12px; background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 50px; margin: 0 4px;'>" +
            "                          <span style='color: rgba(255, 255, 255, 0.9); font-size: 12px;'>Verified</span>" +
            "                        </td>" +
            "                        <td style='padding: 8px 12px; background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 50px; margin: 0 4px;'>" +
            "                          <span style='color: rgba(255, 255, 255, 0.9); font-size: 12px;'>Secure</span>" +
            "                        </td>" +
            "                      </tr>" +
            "                    </table>" +
            "                  </td>" +
            "                </tr>" +
            "              </table>" +
            "              <p style='margin: 0; color: rgba(255, 255, 255, 0.5); font-size: 12px;'>© " + currentYear + " College Attendance Automation. All rights reserved.</p>" +
            "              <p style='margin: 12px 0 0; color: rgba(255, 255, 255, 0.4); font-size: 11px; line-height: 1.5;'>This is an automated message. Please do not reply to this email.</p>" +
            "            </td>" +
            "          </tr>" +
            "        </table>" +
            "      </td>" +
            "    </tr>" +
            "  </table>" +
            "</body>" +
            "</html>";
    }



    /**
     * Inner class to store OTP with expiration
     */
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}
