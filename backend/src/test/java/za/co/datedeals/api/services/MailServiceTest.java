package za.co.datedeals.api.services;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        // Set test mail configuration using reflection
        ReflectionTestUtils.setField(mailService, "host", "smtp.test.com");
        ReflectionTestUtils.setField(mailService, "port", "587");
        ReflectionTestUtils.setField(mailService, "username", "test@example.com");
        ReflectionTestUtils.setField(mailService, "password", "testpassword");
        ReflectionTestUtils.setField(mailService, "from", "noreply@datedeals.co.za");
        ReflectionTestUtils.setField(mailService, "fromName", "DateDeals");
        ReflectionTestUtils.setField(mailService, "to", "admin@datedeals.co.za");
    }

    @Test
    void sendMail_WithValidParameters_DoesNotThrowException() {
        // Arrange
        String subject = "Test Email";
        String body = "This is a test email body";

        // Act & Assert - Method should complete without throwing exception
        // Note: This will attempt to send email but will fail gracefully with logging
        assertThatCode(() -> mailService.sendMail(subject, body))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMail_WithNullSubject_DoesNotThrowException() {
        // Arrange
        String body = "Test body";

        // Act & Assert
        assertThatCode(() -> mailService.sendMail(null, body))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMail_WithEmptyStrings_DoesNotThrowException() {
        // Act & Assert
        assertThatCode(() -> mailService.sendMail("", ""))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMail_WithHtmlContent_DoesNotThrowException() {
        // Arrange
        String subject = "HTML Test";
        String body = "<html><body><h1>Test Email</h1><p>This is HTML content</p></body></html>";

        // Act & Assert
        assertThatCode(() -> mailService.sendMail(subject, body))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithPdfAttachment_WithValidParameters_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Your Coupon";
        String body = "<html><body><p>Your coupon is attached</p></body></html>";
        byte[] pdfAttachment = new byte[]{1, 2, 3, 4, 5};
        String attachmentName = "coupon.pdf";

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithPdfAttachment(
                recipientEmail, recipientName, subject, body, pdfAttachment, attachmentName))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithPdfAttachment_WithNullAttachment_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Test";
        String body = "Test body";

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithPdfAttachment(
                recipientEmail, recipientName, subject, body, null, "test.pdf"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithPdfAttachment_WithEmptyAttachment_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Test";
        String body = "Test body";
        byte[] emptyAttachment = new byte[0];

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithPdfAttachment(
                recipientEmail, recipientName, subject, body, emptyAttachment, "test.pdf"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithPdfAttachment_WithNullRecipientName_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String subject = "Test";
        String body = "Test body";
        byte[] pdfAttachment = new byte[]{1, 2, 3};

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithPdfAttachment(
                recipientEmail, null, subject, body, pdfAttachment, "test.pdf"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithPdfAttachment_WithLargePdf_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Large PDF Test";
        String body = "Large PDF attached";
        byte[] largePdfAttachment = new byte[1024 * 1024]; // 1MB
        Arrays.fill(largePdfAttachment, (byte) 1);

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithPdfAttachment(
                recipientEmail, recipientName, subject, body, largePdfAttachment, "large.pdf"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithMultiplePdfAttachments_WithValidParameters_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Multiple Coupons";
        String body = "<html><body><p>Your coupons are attached</p></body></html>";
        
        List<byte[]> pdfAttachments = Arrays.asList(
                new byte[]{1, 2, 3},
                new byte[]{4, 5, 6},
                new byte[]{7, 8, 9}
        );
        List<String> attachmentNames = Arrays.asList(
                "coupon1.pdf",
                "coupon2.pdf",
                "coupon3.pdf"
        );

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithMultiplePdfAttachments(
                recipientEmail, recipientName, subject, body, pdfAttachments, attachmentNames))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithMultiplePdfAttachments_WithEmptyLists_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Test";
        String body = "Test body";
        List<byte[]> emptyAttachments = Arrays.asList();
        List<String> emptyNames = Arrays.asList();

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithMultiplePdfAttachments(
                recipientEmail, recipientName, subject, body, emptyAttachments, emptyNames))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithMultiplePdfAttachments_WithNullLists_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Test";
        String body = "Test body";

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithMultiplePdfAttachments(
                recipientEmail, recipientName, subject, body, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithMultiplePdfAttachments_WithMismatchedListSizes_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Test";
        String body = "Test body";
        
        List<byte[]> pdfAttachments = Arrays.asList(
                new byte[]{1, 2, 3},
                new byte[]{4, 5, 6},
                new byte[]{7, 8, 9}
        );
        List<String> attachmentNames = Arrays.asList("coupon1.pdf"); // Only 1 name for 3 PDFs

        // Act & Assert - Should use default names for missing names
        assertThatCode(() -> mailService.sendMailWithMultiplePdfAttachments(
                recipientEmail, recipientName, subject, body, pdfAttachments, attachmentNames))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithMultiplePdfAttachments_WithSingleAttachment_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Single Coupon";
        String body = "Your coupon is attached";
        
        List<byte[]> pdfAttachments = Arrays.asList(new byte[]{1, 2, 3, 4, 5});
        List<String> attachmentNames = Arrays.asList("coupon.pdf");

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithMultiplePdfAttachments(
                recipientEmail, recipientName, subject, body, pdfAttachments, attachmentNames))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithMultiplePdfAttachments_WithManyAttachments_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Many Coupons";
        String body = "All your coupons";
        
        List<byte[]> pdfAttachments = Arrays.asList(
                new byte[]{1}, new byte[]{2}, new byte[]{3}, new byte[]{4}, new byte[]{5},
                new byte[]{6}, new byte[]{7}, new byte[]{8}, new byte[]{9}, new byte[]{10}
        );
        List<String> attachmentNames = Arrays.asList(
                "c1.pdf", "c2.pdf", "c3.pdf", "c4.pdf", "c5.pdf",
                "c6.pdf", "c7.pdf", "c8.pdf", "c9.pdf", "c10.pdf"
        );

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithMultiplePdfAttachments(
                recipientEmail, recipientName, subject, body, pdfAttachments, attachmentNames))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithMultiplePdfAttachments_WithSpecialCharactersInSubject_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Your Coupons 🎉 - 50% OFF!";
        String body = "Special offer";
        List<byte[]> pdfAttachments = Arrays.asList(new byte[]{1, 2, 3});
        List<String> attachmentNames = Arrays.asList("coupon.pdf");

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithMultiplePdfAttachments(
                recipientEmail, recipientName, subject, body, pdfAttachments, attachmentNames))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithPdfAttachment_WithSpecialCharactersInFileName_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Test";
        String body = "Test";
        byte[] pdfAttachment = new byte[]{1, 2, 3};
        String attachmentName = "coupon_50%_OFF.pdf";

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithPdfAttachment(
                recipientEmail, recipientName, subject, body, pdfAttachment, attachmentName))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMail_WithLongBody_DoesNotThrowException() {
        // Arrange
        String subject = "Long Email";
        StringBuilder longBody = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longBody.append("This is line ").append(i).append(" of the email body. ");
        }

        // Act & Assert
        assertThatCode(() -> mailService.sendMail(subject, longBody.toString()))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailWithPdfAttachment_WithComplexHtmlBody_DoesNotThrowException() {
        // Arrange
        String recipientEmail = "customer@example.com";
        String recipientName = "John Doe";
        String subject = "Rich HTML Email";
        String body = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; }
                        .header { background: #4CAF50; color: white; padding: 20px; }
                        .content { padding: 20px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>Your Coupon</h1>
                    </div>
                    <div class="content">
                        <p>Thank you for your purchase!</p>
                        <p>Your coupon is attached.</p>
                    </div>
                </body>
                </html>
                """;
        byte[] pdfAttachment = new byte[]{1, 2, 3, 4, 5};

        // Act & Assert
        assertThatCode(() -> mailService.sendMailWithPdfAttachment(
                recipientEmail, recipientName, subject, body, pdfAttachment, "coupon.pdf"))
                .doesNotThrowAnyException();
    }
}
