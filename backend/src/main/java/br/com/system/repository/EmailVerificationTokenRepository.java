package br.com.system.repository;

import br.com.system.model.EmailVerificationToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EmailVerificationToken> findFirstByTokenHashAndConsumedAtIsNullAndExpiresAtAfter(
            String tokenHash, LocalDateTime now);

    Optional<EmailVerificationToken> findTopByAdministratorIdOrderByCreatedAtDesc(Long administratorId);

    @Modifying
    @Query("UPDATE EmailVerificationToken token SET token.consumedAt = :now " +
            "WHERE token.administrator.id = :administratorId AND token.consumedAt IS NULL")
    void consumeAllByAdministratorId(@Param("administratorId") Long administratorId,
                                     @Param("now") LocalDateTime now);
}
