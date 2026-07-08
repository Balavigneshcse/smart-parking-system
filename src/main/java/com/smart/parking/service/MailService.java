package com.smart.parking.service;

import com.smart.parking.config.AppProperties;
import com.smart.parking.domain.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public MailService(JavaMailSender mailSender, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
    }

    public void sendReservationConfirmation(Reservation reservation, String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }
        String body = """
                Dear %s,

                Your parking reservation is confirmed.

                Reference: %s
                Place: %s
                Space: %s
                Vehicle: %s
                From: %s
                To: %s
                Quoted amount: INR %s
                Payment mode: %s

                Thank you for using Smart Parking System.
                """.formatted(
                reservation.getVehicle().getOwnerName(),
                reservation.getReference(),
                reservation.getSpace().getZone().getPlace().getName(),
                reservation.getSpace().getCode(),
                reservation.getVehicle().getPlateNumber(),
                reservation.getStartsAt(),
                reservation.getEndsAt(),
                reservation.getQuotedAmount(),
                reservation.getPaymentMode());

        if (!appProperties.getMail().isEnabled()) {
            log.info("Mail disabled — reservation confirmation for {}:\n{}", recipientEmail, body);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appProperties.getMail().getFrom());
        message.setTo(recipientEmail);
        message.setSubject("Parking Reservation Confirmed — " + reservation.getReference());
        message.setText(body);
        mailSender.send(message);
    }
}
