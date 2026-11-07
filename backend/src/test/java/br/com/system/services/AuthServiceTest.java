package br.com.system.services;

import br.com.system.data.dto.request.LoginRequestDTO;
import br.com.system.data.dto.response.LoginResponseDTO;
import br.com.system.exception.BusinessException;
import br.com.system.model.Administrator;
import br.com.system.model.Company;
import br.com.system.model.CompanyMembership;
import br.com.system.model.RefreshToken;
import br.com.system.model.UserEntity;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.CompanyMembershipRepository;
import br.com.system.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdministratorRepository administratorRepository;

    @Mock
    private CompanyMembershipRepository companyMembershipRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRejectLoginWhenAdministratorDoesNotExist() {
        LoginRequestDTO request = loginRequest();
        when(administratorRepository.findByLogin("admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid login or password!");
    }

    @Test
    void shouldRejectLoginWhenPasswordDoesNotMatch() {
        LoginRequestDTO request = loginRequest();
        Administrator administrator = administrator();
        when(administratorRepository.findByLogin("admin")).thenReturn(Optional.of(administrator));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid login or password!");
    }

    @Test
    void shouldGenerateAccessAndRefreshTokensForValidLogin() {
        ReflectionTestUtils.setField(authService, "expiration", 60_000L);
        LoginRequestDTO request = loginRequest();
        Administrator administrator = administrator();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        when(administratorRepository.findByLogin("admin")).thenReturn(Optional.of(administrator));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(companyMembershipRepository.findByUserIdAndActiveTrueOrderByIdAsc(1L))
                .thenReturn(List.of(membership(administrator)));
        when(tokenProvider.generateToken("admin", 1L, 1L, 1L)).thenReturn("access-token");
        when(refreshTokenService.generate(administrator)).thenReturn(refreshToken);

        LoginResponseDTO response = authService.login(request);

        assertThat(response.getLogin()).isEqualTo("admin");
        assertThat(response.getAuthenticated()).isTrue();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getCompanyId()).isEqualTo(1L);
        assertThat(administrator.getLastLogin()).isNotNull();
        verify(administratorRepository).save(administrator);
    }

    @Test
    void shouldRefreshAccessTokenAndRevokeTokenOnLogout() {
        ReflectionTestUtils.setField(authService, "expiration", 60_000L);
        Administrator administrator = administrator();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setAdministrator(administrator);
        when(refreshTokenService.validate("refresh-token")).thenReturn(refreshToken);
        when(companyMembershipRepository.findByUserIdAndActiveTrueOrderByIdAsc(1L))
                .thenReturn(List.of(membership(administrator)));
        when(tokenProvider.generateToken("admin", 1L, 1L, 1L)).thenReturn("new-access-token");

        LoginResponseDTO response = authService.refreshToken("refresh-token");
        authService.logout("refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenService).revoke("refresh-token");
    }

    private LoginRequestDTO loginRequest() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLogin("admin");
        request.setPassword("password");
        return request;
    }

    private Administrator administrator() {
        Administrator administrator = new Administrator();
        UserEntity user = new UserEntity();
        user.setId(1L);
        administrator.setLogin("admin");
        administrator.setPassword("encoded-password");
        administrator.setUser(user);
        return administrator;
    }

    private CompanyMembership membership(Administrator administrator) {
        Company company = new Company();
        company.setId(1L);
        company.setTradeName("Empresa padrão");
        company.setActive(true);

        CompanyMembership membership = new CompanyMembership();
        membership.setId(1L);
        membership.setUser(administrator.getUser());
        membership.setCompany(company);
        membership.setActive(true);
        return membership;
    }
}
