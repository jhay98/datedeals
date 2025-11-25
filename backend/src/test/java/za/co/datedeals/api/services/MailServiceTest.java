package za.co.datedeals.api.services;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmail_WithValidData_SendsEmail() {
        // Arrange
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        String body = "<html><body>Test Body</body></html>";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        mailService.sendEmail(toEmail, subject, body);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_WithException_HandlesGracefully() {
        // Arrange
        String toEmail = "invalid@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(MimeMessage.class));

        // Act - should not throw exception
        mailService.sendEmail(toEmail, subject, body);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_WithHtmlContent_SendsHtmlEmail() {
        // Arrange
        String toEmail = "html@example.com";
        String subject = "HTML Email";
        String htmlBody = "<html><body><h1>Welcome</h1><p>This is HTML content</p></body></html>";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        mailService.sendEmail(toEmail, subject, htmlBody);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
}
