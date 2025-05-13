package com.cq.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;

    public void sendTestEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("lethithanhtu399@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Test Email");
            message.setText("This is a test email from the system.");
            
            mailSender.send(message);
            System.out.println("Test email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("Error sending test email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}