package br.com.system.repository;

import br.com.system.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Page<Client> findByCompanyId(Long companyId, Pageable pageable);

    Optional<Client> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndDocumentNumber(Long companyId, String documentNumber);

    boolean existsByCompanyIdAndDocumentNumberAndIdNot(Long companyId, String documentNumber, Long id);

    boolean existsByCompanyIdAndUserEmail(Long companyId, String email);

    boolean existsByCompanyIdAndUserEmailAndIdNot(Long companyId, String email, Long id);
}
