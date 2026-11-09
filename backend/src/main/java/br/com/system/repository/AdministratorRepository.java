package br.com.system.repository;

import br.com.system.model.Administrator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Optional<Administrator> findByLogin(String login);
    Optional<Administrator> findByLoginIgnoreCase(String login);
    Optional<Administrator> findByUserId(Long userId);
    @Query("SELECT administrator FROM Administrator administrator JOIN FETCH administrator.user user " +
            "WHERE LOWER(user.email) = LOWER(:email)")
    Optional<Administrator> findByUserEmailIgnoreCase(@Param("email") String email);
    boolean existsByLogin(String login);
    boolean existsByLoginAndIdNot(String login, Long id);
}
