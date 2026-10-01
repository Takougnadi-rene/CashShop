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

/**
 * Sends e-mails (currently the credentials of a newly created employee account) through an SMTP server
 * configured with CASH_SHOP_SMTP_* environment variables.
 */
public class EmailService {
    /**
     * Sends the login and temporary password to the employee. Returns false when SMTP is not configured or the
     * e-mail cannot be sent.
     */
    public boolean sendEmployeeCredentials(String email, String login, String password) {
        // SMTP settings come from environment variables; when one is missing, no e-mail is sent.
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
            // Credentials used to authenticate against the SMTP server.
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

    /** Null-safe check for empty or whitespace-only strings. */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}