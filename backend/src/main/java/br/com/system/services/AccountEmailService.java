package br.com.system.services;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AccountEmailService {
    private static final Logger LOGGER = Logger.getLogger(AccountEmailService.class.getName());
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String frontendUrl;
    private final String fromAddress;

    public AccountEmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                               @Value("${app.frontend-url}") String frontendUrl,
                               @Value("${app.mail-from}") String fromAddress) {
        this.mailSenderProvider = mailSenderProvider;
        this.frontendUrl = frontendUrl.replaceAll("/$", "");
        this.fromAddress = fromAddress;
    }

    @Async
    public void sendPasswordResetEmail(String recipient, String token) {
        send(recipient, "Redefinição de senha do Estocaí",
                "Recebemos uma solicitação para redefinir sua senha.\n\n" +
                        "Use este link em até 30 minutos e apenas uma vez:\n" +
                        frontendUrl + "/reset-password?token=" + token + "\n\n" +
                        "Se você não solicitou a redefinição, ignore este e-mail.");
    }

    @Async
    public void sendVerificationEmail(String recipient, String token) {
        send(recipient, "Confirme seu e-mail do Estocaí",
                "Sua empresa está quase pronta no Estocaí.\n\n" +
                        "Confirme seu e-mail em até 24 horas usando este link:\n" +
                        frontendUrl + "/verify-email?token=" + token + "\n\n" +
                        "Se você não criou esta conta, ignore este e-mail.");
    }

    private void send(String recipient, String subject, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            LOGGER.warning("Account email is not configured; no SMTP sender is available.");
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            LOGGER.log(Level.SEVERE, "Unable to send account email.", exception);
        }
    }
}
