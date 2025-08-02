package com.soloproject.LegalPark.helper;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String email;


    public void sendEmail(String toEmail, String subject, String body) {
        int attempts = 0;
        boolean sent = false;

        while (attempts < 3 && !sent) {
            try {
                MimeMessage message = emailSender.createMimeMessage();
                message.setFrom( "TEAM TECH" + "<"+ email + ">");
                message.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(toEmail));
                message.setContent(body,"text/html; charset=utf-8");
                message.setSubject(subject);
                emailSender.send(message);
                sent = true;

            } catch (Exception e) {
                attempts++;
                if (attempts == 3) {
                    System.err.println("All attempts to send email failed.");
                }
            }
        }
    }

}
