package br.com.system.repository;

import br.com.system.model.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
            SELECT DISTINCT u FROM UserEntity u
            WHERE EXISTS (
                SELECT 1 FROM CompanyMembership membership
                WHERE membership.user = u
                  AND membership.company.id = :companyId
                  AND membership.active = true
            ) OR EXISTS (
                SELECT 1 FROM Client client
                WHERE client.user = u
                  AND client.company.id = :companyId
            )
            ORDER BY u.id
            """)
    List<UserEntity> findAllByCompanyId(@Param("companyId") Long companyId);

    @Query("""
            SELECT u FROM UserEntity u
            WHERE u.id = :id AND (
                EXISTS (
                    SELECT 1 FROM CompanyMembership membership
                    WHERE membership.user = u
                      AND membership.company.id = :companyId
                      AND membership.active = true
                ) OR EXISTS (
                    SELECT 1 FROM Client client
                    WHERE client.user = u
                      AND client.company.id = :companyId
                )
            )
            """)
    Optional<UserEntity> findByIdAndCompanyId(
            @Param("id") Long id,
            @Param("companyId") Long companyId);
}
