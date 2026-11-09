package br.com.system.repository;

import br.com.system.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findFirstByTokenHashAndConsumedAtIsNullAndExpiresAtAfter(
            String tokenHash, LocalDateTime now);

    Optional<PasswordResetToken> findTopByAdministratorIdOrderByCreatedAtDesc(Long administratorId);

    @Modifying
    @Query("UPDATE PasswordResetToken token SET token.consumedAt = :now " +
            "WHERE token.administrator.id = :administratorId AND token.consumedAt IS NULL")
    void consumeAllByAdministratorId(@Param("administratorId") Long administratorId,
                                    @Param("now") LocalDateTime now);
}
