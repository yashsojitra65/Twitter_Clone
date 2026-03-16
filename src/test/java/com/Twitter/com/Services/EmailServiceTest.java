package com.Twitter.com.Services;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Test
    void sendOtpEmailBuildsAndSendsMessage() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        EmailService emailService = new EmailService(mailSender);
        setPrivateField(emailService, "javaMailSender", mailSender);
        setPrivateField(emailService, "fromEmail", "sender@example.com");

        emailService.sendOtpEmail("user@example.com", "123456");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage sent = captor.getValue();

        assertNotNull(sent);
        assertEquals("OTP Verification", sent.getSubject());
        assertEquals("user@example.com", sent.getAllRecipients()[0].toString());
        assertEquals("sender@example.com", sent.getFrom()[0].toString());
    }

    private static void setPrivateField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
