package za.co.datedeals.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
// import lombok.Value;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;
    private Logger logger = LoggerFactory.getLogger(MailService.class);

    public void sendEmail(String toEmail, String subject, String body){
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("Date Deals <noreply@datedeals.co.za>");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (Exception e) { 
            logger.error(e.toString());
        }

    }

    
}
