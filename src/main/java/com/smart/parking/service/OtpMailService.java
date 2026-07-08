package com.smart.parking.service;

import com.smart.parking.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OtpMailService {

    private static final Logger log = LoggerFactory.getLogger(OtpMailService.class);

    private final JavaMailSender mailSender;
    private final AppProperties props;

    public OtpMailService(JavaMailSender mailSender, AppProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    @Async
    public void sendOtp(String toEmail, String otp, String userName) {
        String body = """
                Dear %s,

                Your Smart Parking password reset OTP is:

                        %s

                This OTP is valid for 10 minutes.
                If you did not request a password reset, please ignore this email.

                — Smart Parking System
                """.formatted(userName != null ? userName : "User", otp);

        if (!props.getMail().isEnabled()) {
            log.info("Mail disabled — OTP for {} is: {}", toEmail, otp);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(props.getMail().getFrom());
            msg.setTo(toEmail);
            msg.setSubject("Smart Parking — Password Reset OTP");
            msg.setText(body);
            mailSender.send(msg);
            log.info("OTP sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }
}
