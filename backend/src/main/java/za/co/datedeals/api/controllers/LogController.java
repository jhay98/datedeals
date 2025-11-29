package za.co.datedeals.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.co.datedeals.api.services.MailService;
import za.co.datedeals.api.utils.ShopifyWebhookVerifier;

@RestController
@RequestMapping("/log")
public class LogController {
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    private static final String HMAC_HEADER = "x-shopify-hmac-sha256";

    @Autowired
    private MailService mailService;

    @Value("${shopify.webhook.secret}")
    private String webhookSecret;
    
    @PostMapping(path = "/log-request")
    public String logRequest(@RequestBody String entity, @RequestHeader Map<String, String> headers) {
        logger.info("Body: {}", entity);
        logger.info("Headers: {}", headers);
        
        String hmacHeader = headers.get(HMAC_HEADER);
        boolean isValid = ShopifyWebhookVerifier.verifyWebhook(entity, hmacHeader, webhookSecret);
        logger.info("HMAC Verification Result: {}", isValid);

     
        // try {
        //     mailService.sendMail("hi", "hi there");
        // } catch (Exception e) {
        //     logger.error("Failed to send test email", e);
        // }
        return entity;
    }
}
