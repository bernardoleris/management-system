package br.com.system.services;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EmailVerificationEmailListener {
    private final AccountEmailService emailService;

    public EmailVerificationEmailListener(AccountEmailService emailService) {
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendVerificationEmail(EmailVerificationRequestedEvent event) {
        emailService.sendVerificationEmail(event.email(), event.token());
    }
}
