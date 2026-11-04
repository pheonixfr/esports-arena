
package EsportsArena.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String recipient, String token) {
        String verificationLink =
                baseUrl + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipient);
        message.setSubject("Verify your eSports Arena account");
        message.setText(
                "Welcome to eSports Arena!\n\n" +
                "Click the link below to verify your email address:\n\n" +
                verificationLink + "\n\n" +
                "This link expires in 30 minutes.\n\n" +
                "If you did not create this account, you can ignore this email."
        );

        mailSender.send(message);
    }
}
