package br.com.system.repository;

import br.com.system.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Page<Supplier> findByCompanyId(Long companyId, Pageable pageable);

    Page<Supplier> findByCompanyIdAndActiveTrue(Long companyId, Pageable pageable);

    Page<Supplier> findByCompanyIdAndActiveFalse(Long companyId, Pageable pageable);

    Optional<Supplier> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndCnpj(Long companyId, String cnpj);

    boolean existsByCompanyIdAndCnpjAndIdNot(Long companyId, String cnpj, Long id);
}
