package za.co.datedeals.api.entities.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.co.datedeals.api.services.MailService;

@RestController
@RequestMapping("/log")
public class LogController {
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    @Autowired
    private MailService mailService;
    
    @PostMapping(path = "/log-request")
    public String logRequest(@RequestBody String entity) {
        logger.info(entity);

        mailService.sendEmail(
            "johanhay98@gmail.com",
            "New Request Logged",
            "<h1>Request Received</h1><p>" + entity + "</p>"
        );
        return entity;
    }
}
