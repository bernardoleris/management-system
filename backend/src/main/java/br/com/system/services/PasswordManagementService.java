package br.com.system.services;

import br.com.system.exception.BusinessException;
import br.com.system.model.Administrator;
import br.com.system.model.PasswordResetToken;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.PasswordResetTokenRepository;
import br.com.system.repository.RefreshTokenRepository;
import br.com.system.security.TenantPrincipal;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.logging.Logger;

@Service
public class PasswordManagementService {
    private static final Logger LOGGER = Logger.getLogger(PasswordManagementService.class.getName());
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_EXPIRATION_MINUTES = 30;
    private static final int REQUEST_COOLDOWN_MINUTES = 1;
    private static final String INVALID_RESET_TOKEN = "This password reset link is invalid or expired.";

    private final AdministratorRepository administratorRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public PasswordManagementService(
            AdministratorRepository administratorRepository,
            PasswordResetTokenRepository resetTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher) {
        this.administratorRepository = administratorRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        Long userId = currentPrincipal().getUserId();
        Administrator administrator = administratorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Authenticated account was not found."));

        if (!passwordEncoder.matches(currentPassword, administrator.getPassword())) {
            throw new BusinessException("Current password is incorrect.");
        }
        updatePassword(administrator, newPassword);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        administratorRepository.findByUserEmailIgnoreCase(email.trim()).ifPresent(administrator -> {
            LocalDateTime now = LocalDateTime.now();
            if (administrator.getUser().getActive() == null || !administrator.getUser().getActive()) {
                return;
            }

            boolean recentlyRequested = resetTokenRepository
                    .findTopByAdministratorIdOrderByCreatedAtDesc(administrator.getId())
                    .map(token -> token.getCreatedAt() != null &&
                            token.getCreatedAt().isAfter(now.minusMinutes(REQUEST_COOLDOWN_MINUTES)))
                    .orElse(false);
            if (recentlyRequested) return;

            String rawToken = newResetToken();
            resetTokenRepository.consumeAllByAdministratorId(administrator.getId(), now);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setAdministrator(administrator);
            resetToken.setTokenHash(hashToken(rawToken));
            resetToken.setExpiresAt(now.plusMinutes(TOKEN_EXPIRATION_MINUTES));
            resetTokenRepository.save(resetToken);

            eventPublisher.publishEvent(new PasswordResetRequestedEvent(
                    administrator.getUser().getEmail(), rawToken));
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken resetToken = resetTokenRepository
                .findFirstByTokenHashAndConsumedAtIsNullAndExpiresAtAfter(hashToken(rawToken), now)
                .orElseThrow(() -> new BusinessException(INVALID_RESET_TOKEN));

        Administrator administrator = resetToken.getAdministrator();
        if (administrator.getUser().getActive() == null || !administrator.getUser().getActive()) {
            throw new BusinessException(INVALID_RESET_TOKEN);
        }
        updatePassword(administrator, newPassword);
    }

    private void updatePassword(Administrator administrator, String newPassword) {
        if (newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("New password must be at most 72 bytes when encoded as UTF-8.");
        }
        administrator.setPassword(passwordEncoder.encode(newPassword));
        int currentTokenVersion = administrator.getTokenVersion() == null ? 0 : administrator.getTokenVersion();
        administrator.setTokenVersion(currentTokenVersion + 1);
        administratorRepository.save(administrator);
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.revokeAllByAdministratorId(administrator.getId());
        resetTokenRepository.consumeAllByAdministratorId(administrator.getId(), now);
        LOGGER.info("Password updated and refresh sessions revoked for administrator id " + administrator.getId());
    }

    private TenantPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof TenantPrincipal principal) {
            return principal;
        }
        throw new BusinessException("Authentication is required.");
    }

    private String newResetToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }
}
