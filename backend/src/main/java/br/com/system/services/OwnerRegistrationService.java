package br.com.system.services;

import br.com.system.data.dto.request.OwnerRegistrationRequestDTO;
import br.com.system.enums.CompanyRole;
import br.com.system.exception.BusinessException;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.model.*;
import br.com.system.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
public class OwnerRegistrationService {
    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_EXPIRATION_HOURS = 24;
    private static final int REQUEST_COOLDOWN_MINUTES = 1;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String INVALID_TOKEN = "Este link de confirmação é inválido ou expirou.";

    private final AdministratorRepository administratorRepository;
    private final CompanyRepository companyRepository;
    private final UserEntityRepository userRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public OwnerRegistrationService(AdministratorRepository administratorRepository,
                                    CompanyRepository companyRepository,
                                    UserEntityRepository userRepository,
                                    CompanyMembershipRepository membershipRepository,
                                    EmailVerificationTokenRepository verificationTokenRepository,
                                    PasswordEncoder passwordEncoder,
                                    ApplicationEventPublisher eventPublisher) {
        this.administratorRepository = administratorRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void register(OwnerRegistrationRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        String login = dto.getLogin().trim();
        if (administratorRepository.existsByLogin(login)) {
            throw new DuplicateResourceException("Este login já está em uso.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Este e-mail já está em uso.");
        }
        if (dto.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("A senha deve ter no máximo 72 bytes em UTF-8.");
        }

        String companyName = dto.getCompanyName().trim();
        Company company = new Company();
        company.setLegalName(companyName);
        company.setTradeName(companyName);
        company.setEmail(email);
        company.setSlug(uniqueSlug(companyName));
        company.setActive(false);
        companyRepository.save(company);

        UserEntity user = new UserEntity();
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setEmail(email);
        user.setActive(false);
        userRepository.save(user);

        Administrator administrator = new Administrator();
        administrator.setUser(user);
        administrator.setLogin(login);
        administrator.setPassword(passwordEncoder.encode(dto.getPassword()));
        administratorRepository.save(administrator);

        CompanyMembership membership = new CompanyMembership();
        membership.setCompany(company);
        membership.setUser(user);
        membership.setRole(CompanyRole.COMPANY_OWNER);
        membership.setActive(false);
        membershipRepository.save(membership);

        sendVerification(administrator, membership);
    }

    @Transactional
    public void verify(String rawToken) {
        LocalDateTime now = LocalDateTime.now();
        EmailVerificationToken token = verificationTokenRepository
                .findFirstByTokenHashAndConsumedAtIsNullAndExpiresAtAfter(hashToken(rawToken), now)
                .orElseThrow(() -> new BusinessException(INVALID_TOKEN));

        Administrator administrator = token.getAdministrator();
        CompanyMembership membership = token.getCompanyMembership();
        if (Boolean.TRUE.equals(administrator.getUser().getActive()) || Boolean.TRUE.equals(membership.getActive())) {
            throw new BusinessException(INVALID_TOKEN);
        }
        token.setConsumedAt(now);
        administrator.getUser().setActive(true);
        membership.setActive(true);
        membership.getCompany().setActive(true);
        verificationTokenRepository.consumeAllByAdministratorId(administrator.getId(), now);
    }

    @Transactional
    public void resend(String email) {
        administratorRepository.findByUserEmailIgnoreCase(email.trim()).ifPresent(administrator -> {
            if (Boolean.TRUE.equals(administrator.getUser().getActive())) return;
            LocalDateTime now = LocalDateTime.now();
            boolean recentlyRequested = verificationTokenRepository
                    .findTopByAdministratorIdOrderByCreatedAtDesc(administrator.getId())
                    .map(token -> token.getCreatedAt() != null &&
                            token.getCreatedAt().isAfter(now.minusMinutes(REQUEST_COOLDOWN_MINUTES)))
                    .orElse(false);
            if (recentlyRequested) return;
            CompanyMembership membership = membershipRepository.findFirstByUserIdAndRoleOrderByIdAsc(
                    administrator.getUser().getId(), CompanyRole.COMPANY_OWNER).orElse(null);
            if (membership == null) return;
            sendVerification(administrator, membership);
        });
    }

    private void sendVerification(Administrator administrator, CompanyMembership membership) {
        LocalDateTime now = LocalDateTime.now();
        String rawToken = newRawToken();
        verificationTokenRepository.consumeAllByAdministratorId(administrator.getId(), now);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setAdministrator(administrator);
        token.setCompanyMembership(membership);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(now.plusHours(TOKEN_EXPIRATION_HOURS));
        verificationTokenRepository.save(token);
        eventPublisher.publishEvent(new EmailVerificationRequestedEvent(administrator.getUser().getEmail(), rawToken));
    }

    private String uniqueSlug(String companyName) {
        String normalized = Normalizer.normalize(companyName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        String base = normalized.isBlank() ? "empresa" : normalized;
        if (base.length() > 60) base = base.substring(0, 60).replaceAll("-$", "");
        String slug;
        do {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        } while (companyRepository.existsBySlug(slug));
        return slug;
    }

    private String newRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível.", exception);
        }
    }
}
