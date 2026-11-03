package br.com.system.repository;

import br.com.system.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByDocumentNumber(String documentNumber);
}
