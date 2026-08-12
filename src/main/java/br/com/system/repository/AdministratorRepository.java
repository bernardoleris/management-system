package br.com.system.repository;

import br.com.system.model.Administrator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Optional<Administrator> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByLoginAndIdNot(String login, Long id);
}
