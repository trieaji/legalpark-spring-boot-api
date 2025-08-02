package com.soloproject.LegalPark.service.notification;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import com.soloproject.LegalPark.exception.ResponseHandler;
import com.soloproject.LegalPark.helper.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class NotificationServiceImpl implements INotificationService{
    // --- Deklarasi Field di Tingkat Kelas ---
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private MailService mailService;


    @Override
    public ResponseEntity<Object> sendEmailNotification(EmailNotificationRequest request) {
        try {
            mailService.sendEmail(request.getTo(), request.getSubject(), request.getBody());

            logger.info("Email notification sent successfully to: {}", request.getTo());
            return ResponseHandler.generateResponseSuccess(HttpStatus.OK, "Email notification sent successfully.", null);

        } catch (Exception e) {

            logger.error("Failed to send email notification to {}: {}", request.getTo(), e.getMessage(), e);
            return ResponseHandler.generateResponseError(HttpStatus.INTERNAL_SERVER_ERROR, "Email sending failed: " + e.getMessage(), "FAILED");
        }
    }
}
