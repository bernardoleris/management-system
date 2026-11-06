package br.com.system.services;

import br.com.system.data.dto.request.LoginRequestDTO;
import br.com.system.data.dto.response.LoginResponseDTO;
import br.com.system.data.dto.response.AvailableCompanyResponseDTO;
import br.com.system.data.dto.response.SwitchCompanyResponseDTO;
import br.com.system.exception.BusinessException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.model.Administrator;
import br.com.system.model.RefreshToken;
import br.com.system.model.CompanyMembership;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.CompanyMembershipRepository;
import br.com.system.security.jwt.JwtTokenProvider;
import br.com.system.security.TenantPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Service
public class AuthService {
    private final Logger logger = Logger.getLogger(AuthService.class.getName());

    private final Long expiration;

    private final AdministratorRepository administratorRepository;

    private final CompanyMembershipRepository companyMembershipRepository;

    private final RefreshTokenService refreshTokenService;

    private final JwtTokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    public AuthService(
            @Value("${security.jwt.expiration}") Long expiration,
            AdministratorRepository administratorRepository,
            CompanyMembershipRepository companyMembershipRepository,
            RefreshTokenService refreshTokenService,
            JwtTokenProvider tokenProvider,
            PasswordEncoder passwordEncoder) {
        this.expiration = expiration;
        this.administratorRepository = administratorRepository;
        this.companyMembershipRepository = companyMembershipRepository;
        this.refreshTokenService = refreshTokenService;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) {
        logger.info("Authenticating administrator: " + dto.getLogin());

        Administrator administrator = administratorRepository.findByLogin(dto.getLogin())
                .orElseThrow(() -> new BusinessException("Invalid login or password!"));

        if (!passwordEncoder.matches(dto.getPassword(), administrator.getPassword())) {
            throw new BusinessException("Invalid login or password!");
        }

        administrator.setLastLogin(LocalDateTime.now());
        administratorRepository.save(administrator);

        CompanyMembership membership = findActiveMembership(administrator);
        String accessToken = generateAccessToken(administrator, membership);
        RefreshToken refreshToken = refreshTokenService.generate(administrator);

        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + expiration);

        return buildLoginResponse(administrator, membership, issuedAt, expirationDate, accessToken, refreshToken.getToken());
    }

    @Transactional
    public LoginResponseDTO refreshToken(String token, Long companyId) {
        logger.info("Refreshing token!");

        RefreshToken refreshToken = refreshTokenService.validate(token);
        Administrator administrator = refreshToken.getAdministrator();

        CompanyMembership membership = findActiveMembership(administrator, companyId);
        String newAccessToken = generateAccessToken(administrator, membership);

        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + expiration);

        return buildLoginResponse(administrator, membership, issuedAt, expirationDate, newAccessToken, token);
    }

    @Transactional
    public LoginResponseDTO refreshToken(String token) {
        return refreshToken(token, null);
    }

    @Transactional
    public void logout(String token) {
        logger.info("Logging out!");
        refreshTokenService.revoke(token);
    }

    @Transactional(readOnly = true)
    public List<AvailableCompanyResponseDTO> availableCompanies() {
        Long userId = currentPrincipal().getUserId();
        return companyMembershipRepository.findByUserIdAndActiveTrueOrderByIdAsc(userId).stream()
                .filter(membership -> Boolean.TRUE.equals(membership.getCompany().getActive()))
                .map(membership -> new AvailableCompanyResponseDTO(
                        membership.getCompany().getId(),
                        membership.getCompany().getTradeName(),
                        membership.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SwitchCompanyResponseDTO switchCompany(Long companyId) {
        TenantPrincipal principal = currentPrincipal();
        CompanyMembership membership = companyMembershipRepository
                .findByCompanyIdAndUserIdAndActiveTrue(companyId, principal.getUserId())
                .filter(value -> Boolean.TRUE.equals(value.getCompany().getActive()))
                .orElseThrow(() -> new BusinessException("No active access to this company!"));
        Administrator administrator = administratorRepository.findByUserId(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Administrator not found!"));

        Date issuedAt = new Date();
        SwitchCompanyResponseDTO response = new SwitchCompanyResponseDTO();
        response.setCompanyId(membership.getCompany().getId());
        response.setCompanyName(membership.getCompany().getTradeName());
        response.setIssuedAt(issuedAt);
        response.setExpiration(new Date(issuedAt.getTime() + expiration));
        response.setAccessToken(generateAccessToken(administrator, membership));
        return response;
    }

    private CompanyMembership findActiveMembership(Administrator administrator) {
        return findActiveMembership(administrator, null);
    }

    private CompanyMembership findActiveMembership(Administrator administrator, Long companyId) {
        List<CompanyMembership> memberships = companyMembershipRepository
                .findByUserIdAndActiveTrueOrderByIdAsc(administrator.getUser().getId());

        return memberships.stream()
                .filter(membership -> Boolean.TRUE.equals(membership.getCompany().getActive()))
                .filter(membership -> companyId == null || membership.getCompany().getId().equals(companyId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No active company access found!"));
    }

    private TenantPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof TenantPrincipal principal) {
            return principal;
        }
        throw new BusinessException("Active company access is required!");
    }

    private String generateAccessToken(Administrator administrator, CompanyMembership membership) {
        return tokenProvider.generateToken(
                administrator.getLogin(),
                administrator.getUser().getId(),
                membership.getCompany().getId(),
                membership.getId()
        );
    }

    private LoginResponseDTO buildLoginResponse(
            Administrator administrator,
            CompanyMembership membership,
            Date issuedAt,
            Date expirationDate,
            String accessToken,
            String refreshToken) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setLogin(administrator.getLogin());
        response.setAuthenticated(true);
        response.setIssuedAt(issuedAt);
        response.setExpiration(expirationDate);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setCompanyId(membership.getCompany().getId());
        response.setCompanyName(membership.getCompany().getTradeName());
        return response;
    }
}
