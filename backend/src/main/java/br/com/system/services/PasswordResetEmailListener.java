package br.com.system.services;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PasswordResetEmailListener {
    private final AccountEmailService emailService;

    public PasswordResetEmailListener(AccountEmailService emailService) {
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPasswordResetEmail(PasswordResetRequestedEvent event) {
        emailService.sendPasswordResetEmail(event.email(), event.token());
    }
}
