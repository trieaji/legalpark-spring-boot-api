package com.soloproject.LegalPark.dto.request.notification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailNotificationRequest {
    @NotBlank(message = "Recipient email cannot be blank")
    @Email(message = "Recipient email must be a valid email address")
    private String to;

    @NotBlank(message = "Subject cannot be blank")
    private String subject;

    @NotBlank(message = "Body cannot be blank")
    private String body;

    private String cc;
    private String bcc;

    public @NotBlank(message = "Recipient email cannot be blank") @Email(message = "Recipient email must be a valid email address") String getTo() {
        return to;
    }

    public void setTo(@NotBlank(message = "Recipient email cannot be blank") @Email(message = "Recipient email must be a valid email address") String to) {
        this.to = to;
    }

    public @NotBlank(message = "Subject cannot be blank") String getSubject() {
        return subject;
    }

    public void setSubject(@NotBlank(message = "Subject cannot be blank") String subject) {
        this.subject = subject;
    }

    public @NotBlank(message = "Body cannot be blank") String getBody() {
        return body;
    }

    public void setBody(@NotBlank(message = "Body cannot be blank") String body) {
        this.body = body;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }
}

