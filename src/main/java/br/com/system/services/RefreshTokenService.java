package br.com.system.services;

import br.com.system.exception.BusinessException;
import br.com.system.model.Administrator;
import br.com.system.model.RefreshToken;
import br.com.system.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class RefreshTokenService {
    private final Logger logger = Logger.getLogger(RefreshTokenService.class.getName());

    @Value("${security.jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken generate(Administrator administrator) {
        logger.info("Generating refresh token!");

        // revoga todos os tokens anteriores do admin
        refreshTokenRepository.revokeAllByAdministratorId(administrator.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setAdministrator(administrator);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validate(String token) {
        logger.info("Validating refresh token!");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid refresh token!"));

        if (refreshToken.getRevoked()) {
            throw new BusinessException("Refresh token has been revoked!");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Refresh token has expired!");
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(String token) {
        logger.info("Revoking refresh token!");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid refresh token!"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}