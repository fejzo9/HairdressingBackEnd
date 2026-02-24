package com.hairbooking.reservation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.frontend.url:http://localhost:5173}") String frontendUrl,
            @Value("${spring.mail.username:fejzo999@gmail.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.frontendUrl = (frontendUrl != null && !frontendUrl.isBlank()) ? frontendUrl : "http://localhost:5173";
        this.fromEmail = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "fejzo999@gmail.com";
    }

    public void sendVerificationEmail(String to, String token) {
        String verificationLink = frontendUrl + "/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Please verify your email address");
        message.setText("Thank you for registering!\n\n"
                + "Please click the link below to verify your email address:\n"
                + verificationLink + "\n\n"
                + "This link will expire in 24 hours.\n\n"
                + "If you did not create an account, please ignore this email.");

        mailSender.send(message);
    }
}
