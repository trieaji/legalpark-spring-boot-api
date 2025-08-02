package com.soloproject.LegalPark.service.notification;

import com.soloproject.LegalPark.dto.request.notification.EmailNotificationRequest;
import org.springframework.http.ResponseEntity;

public interface INotificationService {

//     Mengirim notifikasi email.
    ResponseEntity<Object> sendEmailNotification(EmailNotificationRequest request);

}
