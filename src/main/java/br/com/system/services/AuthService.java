package br.com.system.services;

import br.com.system.data.dto.request.LoginRequestDTO;
import br.com.system.data.dto.response.LoginResponseDTO;
import br.com.system.exception.BusinessException;
import br.com.system.model.Administrator;
import br.com.system.model.RefreshToken;
import br.com.system.repository.AdministratorRepository;
import br.com.system.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.logging.Logger;

@Service
public class AuthService {
    private final Logger logger = Logger.getLogger(AuthService.class.getName());

    @Value("${security.jwt.expiration}")
    private Long expiration;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        String accessToken = tokenProvider.generateToken(dto.getLogin());
        RefreshToken refreshToken = refreshTokenService.generate(administrator);

        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + expiration);

        return new LoginResponseDTO(
                dto.getLogin(),
                true,
                issuedAt,
                expirationDate,
                accessToken,
                refreshToken.getToken()
        );
    }

    @Transactional
    public LoginResponseDTO refreshToken(String token) {
        logger.info("Refreshing token!");

        RefreshToken refreshToken = refreshTokenService.validate(token);
        Administrator administrator = refreshToken.getAdministrator();

        String newAccessToken = tokenProvider.generateToken(administrator.getLogin());

        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + expiration);

        return new LoginResponseDTO(
                administrator.getLogin(),
                true,
                issuedAt,
                expirationDate,
                newAccessToken,
                token
        );
    }

    @Transactional
    public void logout(String token) {
        logger.info("Logging out!");
        refreshTokenService.revoke(token);
    }
}