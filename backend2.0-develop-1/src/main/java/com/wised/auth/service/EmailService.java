package com.wised.auth.service;

import com.wised.auth.dtos.SendTemplateEmailMessageResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * EmailService provides methods for sending email messages, including OTPs and other notifications.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Environment environment;

    /**
     * Sends an email message containing an OTP (One-Time Password) to the specified recipient.
     *
     * @param to      The recipient's email address.
     * @param subject The subject of the email.
     * @param message The message content, which may include the OTP.
     * @throws MessagingException If an error occurs while sending the email.
     */
    public SendTemplateEmailMessageResponse sendTemplateEmailMessage(String to, String subject, String message) {
        try {
            // Create a MimeMessage for HTML content
            MimeMessage msg = javaMailSender.createMimeMessage();

            // Initialize MimeMessageHelper for handling MIME messages
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);

            // Set recipient, subject, and message content
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, true);

            // Set the sender's email address from the Spring Boot configuration
            helper.setFrom(environment.getProperty("spring.mail.username"));

            // Send the email message
            javaMailSender.send(msg);

            // Return a success response
            return SendTemplateEmailMessageResponse.builder()
                    .success(true)
                    .message("Email sent successfully")
                    .build();
        } catch (MessagingException e) {
            // Handle messaging exceptions and return an error response
            return SendTemplateEmailMessageResponse.builder()
                    .success(false)
                    .message("Error sending email check your email, Error: " + e.getMessage())
                    .build();

        }catch (Exception e){
            return SendTemplateEmailMessageResponse.builder()
                    .success(false)
                    .message("Error :" + e.getMessage())
                    .build();
        }
    }


    /**
     * Sends a simple text email message to the specified recipient.
     *
     * @param recipient The recipient's email address.
     * @param sender    The sender's email address.
     * @param subject   The subject of the email.
     * @param otp       The message content, typically an OTP or notification.
     */
    public void sendEmail(String recipient, String sender, String subject, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();

        // Set sender, recipient, subject, and message content
        msg.setFrom(sender);
        msg.setTo(recipient);
        msg.setSubject(subject);
        msg.setText("Hi,\nYour new password is " + otp + "\nThanks.");

        // Send the email message
        javaMailSender.send(msg);
    }
}
