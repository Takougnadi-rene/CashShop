package com.cash_shop.common;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {
    public boolean sendEmployeeCredentials(String email, String login, String password) {
        String host = System.getenv("CASH_SHOP_SMTP_HOST");
        String username = System.getenv("CASH_SHOP_SMTP_USERNAME");
        String smtpPassword = System.getenv("CASH_SHOP_SMTP_PASSWORD");
        String sender = System.getenv("CASH_SHOP_SMTP_FROM");
        if (isBlank(host) || isBlank(username) || isBlank(smtpPassword) || isBlank(sender) || isBlank(email)) {
            return false;
        }

        String port = System.getenv().getOrDefault("CASH_SHOP_SMTP_PORT", "587");
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, smtpPassword);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Your Cash Shop account");
            message.setText("Your application account is ready.\n\nLogin: " + login
                    + "\nTemporary password: " + password
                    + "\n\nPlease change this password after your first login.");
            Transport.send(message);
            return true;
        } catch (MessagingException | IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}