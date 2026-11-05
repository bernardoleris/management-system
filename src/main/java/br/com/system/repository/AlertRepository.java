package br.com.system.repository;

import br.com.system.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByCompanyId(Long companyId);

    List<Alert> findByCompanyIdAndActiveTrue(Long companyId);

    List<Alert> findByCompanyIdAndReadFalseAndActiveTrue(Long companyId);

    List<Alert> findByCompanyIdAndAdminIdAndActiveTrue(Long companyId, Long adminId);

    List<Alert> findByCompanyIdAndProductIdAndActiveTrue(Long companyId, Long productId);

    Optional<Alert> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndProductId(Long companyId, Long productId);
}
