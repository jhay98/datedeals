package za.co.datedeals.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Properties;


@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private String port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${mail.from.address}")
    private String from;

    @Value("${mail.from.name}")
    private String fromName;

    @Value("${mail.recipient.default}")
    private String to;

    @Async
    public void sendMail(String subject, String body) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            logger.info("Email sent successfully to {} with subject: {}", to, subject);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {} with subject: {}. Error: {}", to, subject, e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsupported encoding in email address. Error: {}", e.getMessage(), e);
        }
    }

    /**
     * Sends an email with PDF attachment to a specific recipient
     * 
     * @param recipientEmail Email address of the recipient
     * @param recipientName Name of the recipient (optional)
     * @param subject Email subject
     * @param body Email body (HTML supported)
     * @param pdfAttachment PDF file as byte array
     * @param attachmentName Name for the PDF attachment
     */
    @Async
    public void sendMailWithPdfAttachment(
            String recipientEmail, 
            String recipientName,
            String subject, 
            String body, 
            byte[] pdfAttachment,
            String attachmentName) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            // Create multipart message for attachment
            MimeMultipart multipart = new MimeMultipart();

            // Add body part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(body, "text/html; charset=utf-8");
            multipart.addBodyPart(textPart);

            // Add PDF attachment
            if (pdfAttachment != null && pdfAttachment.length > 0) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setContent(pdfAttachment, "application/pdf");
                attachmentPart.setFileName(attachmentName);
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);

            Transport.send(message);
            logger.info("Email with PDF attachment sent successfully to {} with subject: {}", recipientEmail, subject);
        } catch (MessagingException e) {
            logger.error("Failed to send email with attachment to {} with subject: {}. Error: {}", 
                        recipientEmail, subject, e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsupported encoding in email address. Error: {}", e.getMessage(), e);
        }
    }

    /**
     * Sends an email with multiple PDF attachments to a specific recipient
     * 
     * @param recipientEmail Email address of the recipient
     * @param recipientName Name of the recipient (optional)
     * @param subject Email subject
     * @param body Email body (HTML supported)
     * @param pdfAttachments List of PDF files as byte arrays
     * @param attachmentNames List of names for the PDF attachments
     */
    @Async
    public void sendMailWithMultiplePdfAttachments(
            String recipientEmail, 
            String recipientName,
            String subject, 
            String body, 
            java.util.List<byte[]> pdfAttachments,
            java.util.List<String> attachmentNames) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            // Create multipart message for attachments
            MimeMultipart multipart = new MimeMultipart();

            // Add body part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(body, "text/html; charset=utf-8");
            multipart.addBodyPart(textPart);

            // Add PDF attachments
            if (pdfAttachments != null && !pdfAttachments.isEmpty()) {
                for (int i = 0; i < pdfAttachments.size(); i++) {
                    byte[] pdfData = pdfAttachments.get(i);
                    String fileName = i < attachmentNames.size() ? attachmentNames.get(i) : "coupon_" + (i + 1) + ".pdf";
                    
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.setContent(pdfData, "application/pdf");
                    attachmentPart.setFileName(fileName);
                    multipart.addBodyPart(attachmentPart);
                }
            }

            message.setContent(multipart);

            Transport.send(message);
            logger.info("Email with {} PDF attachments sent successfully to {} with subject: {}", 
                       pdfAttachments.size(), recipientEmail, subject);
        } catch (MessagingException e) {
            logger.error("Failed to send email with attachments to {} with subject: {}. Error: {}", 
                        recipientEmail, subject, e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsupported encoding in email address. Error: {}", e.getMessage(), e);
        }
    }
}