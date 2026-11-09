package br.com.system.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String secret;

    private final Long expiration;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration}") Long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    // ─── Gera o token ─────────────────────────────────────────────────────────

    public String generateToken(String login, Long userId, Long companyId, Long membershipId) {
        return generateToken(login, userId, companyId, membershipId, 0);
    }

    public String generateToken(String login, Long userId, Long companyId, Long membershipId, Integer tokenVersion) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withSubject(login)
                .withClaim("user_id", userId)
                .withClaim("company_id", companyId)
                .withClaim("membership_id", membershipId)
                .withClaim("token_version", tokenVersion)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(algorithm);
    }

    // ─── Valida o token ───────────────────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    // ─── Extrai o login do token ──────────────────────────────────────────────

    public String getLoginFromToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
        return decodedJWT.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return getClaimAsLong(token, "user_id");
    }

    public Long getCompanyIdFromToken(String token) {
        return getClaimAsLong(token, "company_id");
    }

    public Long getMembershipIdFromToken(String token) {
        return getClaimAsLong(token, "membership_id");
    }

    public Integer getTokenVersionFromToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
        return decodedJWT.getClaim("token_version").asInt();
    }

    private Long getClaimAsLong(String token, String claimName) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
        return decodedJWT.getClaim(claimName).asLong();
    }
}
