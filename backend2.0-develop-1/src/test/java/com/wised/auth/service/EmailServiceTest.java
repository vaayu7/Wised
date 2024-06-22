package com.wised.auth.service;

import com.wised.auth.dtos.SendTemplateEmailMessageResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private Environment environment;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSendTemplateEmailMessage_Success() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(environment.getProperty("spring.mail.username")).thenReturn("test@example.com");

        SendTemplateEmailMessageResponse response = emailService.sendTemplateEmailMessage("recipient@example.com", "Test Subject", "Test Message");

        assertTrue(response.isSuccess());
        assertEquals("Email sent successfully", response.getMessage());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void testSendTemplateEmailMessage_MessagingException() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(MessagingException.class).when(javaMailSender).send(any(MimeMessage.class));

        SendTemplateEmailMessageResponse response = emailService.sendTemplateEmailMessage("recipient@example.com", "Test Subject", "Test Message");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().startsWith("Error sending email check your email, Error:"));
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void testSendTemplateEmailMessage_OtherException() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(RuntimeException.class).when(javaMailSender).send(any(MimeMessage.class));

        SendTemplateEmailMessageResponse response = emailService.sendTemplateEmailMessage("recipient@example.com", "Test Subject", "Test Message");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().startsWith("Error :"));
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void testSendEmail() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail("recipient@example.com", "sender@example.com", "Test Subject", "123456");

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
