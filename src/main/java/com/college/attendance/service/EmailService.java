package com.college.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    
    /**
     * Get disclaimer HTML for all emails
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


    
    public void sendAttendanceConfirmation(String toEmail, String sic, boolean isVerification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            
            String subject = isVerification ? 
                "✓ Attendance Verification Successful - " + sic : 
                "✓ Attendance Marked Successfully - " + sic;
            helper.setSubject(subject);
            
            String content = isVerification ?
                "<p style='margin: 0 0 16px; color: rgba(255, 255, 255, 0.95); font-size: 16px; line-height: 1.6;'>" +
                "Your hostel attendance has been <strong style='color: #22c55e;'>verified successfully</strong>.</p>" +
                getDisclaimerHtml() :
                "<p style='margin: 0 0 16px; color: rgba(255, 255, 255, 0.95); font-size: 16px; line-height: 1.6;'>" +
                "Your hostel attendance has been <strong style='color: #22c55e;'>marked successfully</strong>.</p>" +
                getDisclaimerHtml();
            
            String details = 
                "<div style='margin-bottom: 16px;'>" +
                "  <div style='display: flex; align-items: center; margin-bottom: 12px;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>●</span>" +
                "    <div>" +
                "      <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Student ID (SIC)</div>" +
                "      <div style='font-size: 16px; font-weight: 600; color: rgba(255, 255, 255, 0.95);'>" + sic + "</div>" +
                "    </div>" +
                "  </div>" +
                "  <div style='display: flex; align-items: center; margin-bottom: 12px;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>⏱</span>" +
                "    <div>" +
                "      <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>" + 
                (isVerification ? "Verification Time" : "Attendance Time") + "</div>" +
                "      <div style='font-size: 16px; font-weight: 600; color: rgba(255, 255, 255, 0.95);'>" + LocalDateTime.now().format(DATE_FORMATTER) + "</div>" +
                "    </div>" +
                "  </div>" +
                "  <div style='display: flex; align-items: center;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>✓</span>" +
                "    <div>" +
                "      <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Status</div>" +
                "      <div style='display: inline-block; padding: 6px 12px; border-radius: 8px; font-size: 12px; font-weight: 700; text-transform: uppercase; background: rgba(34, 197, 94, 0.2); color: #22c55e;'>Confirmed</div>" +
                "    </div>" +
                "  </div>" +
                "</div>";
            
            String additionalMessage = isVerification ?
                "This is a verification check to ensure your attendance was recorded properly. Your attendance is now fully confirmed." :
                "A verification check will be performed shortly to confirm your attendance. You will receive another confirmation email once verified.";
            
            String htmlContent = buildEmailTemplate(subject, content, details, additionalMessage);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to {} for SIC: {}", toEmail, sic);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {} for SIC: {}", toEmail, sic, e);
        }
    }

    public void sendAttendanceFailure(String toEmail, String sic, boolean isVerification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            
            String type = isVerification ? "Verification" : "Attendance";
            String subject = "X Attendance " + type + " Failed - " + sic;
            helper.setSubject(subject);
            
            String content = 
                "<p style='margin: 0 0 16px; color: rgba(255, 255, 255, 0.95); font-size: 16px; line-height: 1.6;'>" +
                "We were unable to mark your hostel attendance " + (isVerification ? "verification" : "") + ". " +
                "<strong style='color: #ef4444;'>Please try again or contact the hostel administration.</strong></p>" +
                getDisclaimerHtml();
            
            String details = 
                "<div style='margin-bottom: 16px;'>" +
                "  <div style='display: flex; align-items: center; margin-bottom: 12px;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>●</span>" +
                "    <div>" +
                "      <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Student ID (SIC)</div>" +
                "      <div style='font-size: 16px; font-weight: 600; color: rgba(255, 255, 255, 0.95);'>" + sic + "</div>" +
                "    </div>" +
                "  </div>" +
                "  <div style='display: flex; align-items: center; margin-bottom: 12px;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>⏱</span>" +
                "    <div>" +
                "      <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Attempted Time</div>" +
                "      <div style='font-size: 16px; font-weight: 600; color: rgba(255, 255, 255, 0.95);'>" + LocalDateTime.now().format(DATE_FORMATTER) + "</div>" +
                "    </div>" +
                "  </div>" +
                "  <div style='display: flex; align-items: center;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>X</span>" +
                "    <div>" +
                "      <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Status</div>" +
                "      <div style='display: inline-block; padding: 6px 12px; border-radius: 8px; font-size: 12px; font-weight: 700; text-transform: uppercase; background: rgba(239, 68, 68, 0.2); color: #ef4444;'>Failed</div>" +
                "    </div>" +
                "  </div>" +
                "</div>";
            
            String additionalMessage = "Please contact the hostel administration if this issue persists. You may need to mark your attendance manually.";
            
            String htmlContent = buildEmailTemplate(subject, content, details, additionalMessage);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Failure HTML email sent to {} for SIC: {}", toEmail, sic);
        } catch (MessagingException e) {
            log.error("Failed to send failure HTML email to {} for SIC: {}", toEmail, sic, e);
        }
    }

    public void sendChangeNotification(String toEmail, String sic, String changeType, String changeDetails) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            
            String subject = "Account Change Notification - " + changeType;
            helper.setSubject(subject);
            
            String content = 
                "<p style='margin: 0 0 16px; color: rgba(255, 255, 255, 0.95); font-size: 16px; line-height: 1.6;'>" +
                "This is to notify you that a change has been made to your account. " +
                "<strong style='color: #f59e0b;'>Please review the details below.</strong></p>" +
                getDisclaimerHtml();
            
            String details = 
                "<div style='margin-bottom: 16px;'>" +
                "  <div style='display: flex; align-items: center; margin-bottom: 12px;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>●</span>" +
                "    <div>" +
                "      <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Student ID (SIC)</div>" +
                "      <div style='font-size: 16px; font-weight: 600; color: rgba(255, 255, 255, 0.95);'>" + sic + "</div>" +
                "    </div>" +
                "  </div>" +
                "  <div style='display: flex; align-items: center; margin-bottom: 12px;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>⟳</span>" +
                "    <div>" +
                "      <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Change Type</div>" +
                "      <div style='font-size: 16px; font-weight: 600; color: rgba(255, 255, 255, 0.95);'>" + changeType + "</div>" +
                "    </div>" +
                "  </div>" +
                "  <div style='display: flex; align-items: center; margin-bottom: 12px;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>✎</span>" +
                "    <div>" +
                "      <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Details</div>" +
                "      <div style='font-size: 16px; font-weight: 600; color: rgba(255, 255, 255, 0.95);'>" + changeDetails + "</div>" +
                "    </div>" +
                "  </div>" +
                "  <div style='display: flex; align-items: center;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>⏱</span>" +
                "    <div>" +
                "      <div style='font-size: 11px; color: rgba(255, 255, 255, 0.5); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; margin-bottom: 4px;'>Time</div>" +
                "      <div style='font-size: 16px; font-weight: 600; color: rgba(255, 255, 255, 0.95);'>" + LocalDateTime.now().format(DATE_FORMATTER) + "</div>" +
                "    </div>" +
                "  </div>" +
                "</div>";
            
            String additionalMessage = "<strong style='color: #f59e0b;'>⚠ Important:</strong> If you did not make this change or if this was done by mistake, please login immediately and update your information, or contact the administrator.";
            
            String htmlContent = buildEmailTemplate(subject, content, details, additionalMessage);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Change notification HTML email sent to {} for SIC: {} - {}", toEmail, sic, changeType);
        } catch (MessagingException e) {
            log.error("Failed to send change notification HTML email to {} for SIC: {}", toEmail, sic, e);
        }
    }

    public void sendCustomMessage(String toEmail, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            
            String content = 
                "<p style='margin: 0 0 16px; color: rgba(255, 255, 255, 0.95); font-size: 16px; line-height: 1.6;'>" +
                body.replace("\n", "<br>") + "</p>" +
                getDisclaimerHtml();
            
            String details = 
                "<div style='padding: 16px; background: rgba(99, 102, 241, 0.1); border-radius: 12px; border: 1px solid rgba(99, 102, 241, 0.3);'>" +
                "  <div style='display: flex; align-items: center;'>" +
                "    <span style='font-size: 24px; margin-right: 12px;'>●</span>" +
                "    <div style='font-size: 14px; color: rgba(255, 255, 255, 0.75); font-style: italic;'>" +
                "      This message was sent from the Hostel Attendance System Admin Panel" +
                "    </div>" +
                "  </div>" +
                "</div>";
            
            String additionalMessage = "If you have any questions or concerns, please contact the hostel administration.";
            
            String htmlContent = buildEmailTemplate(subject, content, details, additionalMessage);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Custom HTML message sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send custom HTML message to {}", toEmail, e);
        }
    }
    
    private String buildEmailTemplate(String subject, String content, String details, String additionalMessage) {
        int currentYear = LocalDateTime.now().getYear();
        
        return "<!DOCTYPE html>" +
            "<html lang='en'>" +
            "<head>" +
            "  <meta charset='UTF-8'>" +
            "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "  <title>" + subject + "</title>" +
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
            "              <p style='margin: 0 0 24px; color: rgba(255, 255, 255, 0.95); font-size: 16px; line-height: 1.6;'>Dear Student,</p>" +
            "              <div style='margin-bottom: 32px;'>" + content + "</div>" +
            "              <table role='presentation' cellspacing='0' cellpadding='0' border='0' width='100%' style='background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; margin-bottom: 32px;'>" +
            "                <tr><td style='padding: 24px;'>" + details + "</td></tr>" +
            "              </table>" +
            "              <p style='margin: 0 0 24px; color: rgba(255, 255, 255, 0.75); font-size: 15px; line-height: 1.6;'>" + additionalMessage + "</p>" +
            "              <div style='margin-top: 40px; padding-top: 24px; border-top: 1px solid rgba(255, 255, 255, 0.1);'>" +
            "                <p style='margin: 0 0 8px; color: rgba(255, 255, 255, 0.95); font-size: 15px; font-weight: 600;'>Best regards,</p>" +
            "                <p style='margin: 0; color: rgba(255, 255, 255, 0.75); font-size: 15px;'>Hostel Attendance System</p>" +
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
}
