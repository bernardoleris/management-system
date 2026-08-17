package br.com.system.repository;

import br.com.system.enums.SaleStatus;
import br.com.system.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    Page<Sale> findByStatus(SaleStatus status, Pageable pageable);

    Page<Sale> findByAdminId(Long adminId, Pageable pageable);

    Page<Sale> findByClientId(Long clientId, Pageable pageable);
}
