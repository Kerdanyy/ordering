package com.foodics.ordering.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${email.to}")
    private String toEmail;

    /**
     * Sends an email using the configured {@link JavaMailSender}.
     *
     * @param subject The subject of the email.
     * @param body    The body content of the email.
     */
    public void sendEmail(String subject, String body) {
        if (StringUtils.isNotBlank(toEmail)) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } else {
            log.warn("Email sending skipped as \"to.email\" value is blank");
        }
    }
}
