package br.com.system.services;

import br.com.system.exception.BusinessException;
import br.com.system.model.RefreshToken;
import br.com.system.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldRejectUnknownRefreshToken() {
        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validate("unknown-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid refresh token!");
    }

    @Test
    void shouldRejectRevokedRefreshToken() {
        RefreshToken token = validToken();
        token.setRevoked(true);
        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validate("revoked-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token has been revoked!");
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        RefreshToken token = validToken();
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validate("expired-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token has expired!");
    }

    @Test
    void shouldMarkExistingRefreshTokenAsRevoked() {
        RefreshToken token = validToken();
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        refreshTokenService.revoke("valid-token");

        assertThat(token.getRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void shouldRevokePreviousTokensAndCreateNewExpiringToken() {
        br.com.system.model.Administrator administrator = new br.com.system.model.Administrator();
        administrator.setId(7L);
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", 60_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime beforeGeneration = LocalDateTime.now();
        RefreshToken generatedToken = refreshTokenService.generate(administrator);

        verify(refreshTokenRepository).revokeAllByAdministratorId(7L);
        verify(refreshTokenRepository).save(generatedToken);
        assertThat(generatedToken.getToken()).isNotBlank();
        assertThat(generatedToken.getAdministrator()).isSameAs(administrator);
        assertThat(generatedToken.getExpiresAt()).isAfter(beforeGeneration.plusSeconds(59));
    }

    private RefreshToken validToken() {
        RefreshToken token = new RefreshToken();
        token.setRevoked(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(1));
        return token;
    }
}
