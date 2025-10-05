package com.soloproject.LegalPark.service.notification;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import org.springframework.http.ResponseEntity;

public interface INotificationService {

//     Sending email notifications.
    ResponseEntity<Object> sendEmailNotification(EmailNotificationRequest request);

}
