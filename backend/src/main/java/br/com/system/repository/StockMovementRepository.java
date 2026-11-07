package br.com.system.repository;

import br.com.system.enums.MovementType;
import br.com.system.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByCompanyId(Long companyId, Pageable pageable);
    Page<StockMovement> findByCompanyIdAndAdminId(Long companyId, Long adminId, Pageable pageable);
    Page<StockMovement> findByCompanyIdAndSupplierId(Long companyId, Long supplierId, Pageable pageable);
    Page<StockMovement> findByCompanyIdAndType(Long companyId, MovementType type, Pageable pageable);
    Optional<StockMovement> findByIdAndCompanyId(Long id, Long companyId);
    Optional<StockMovement> findBySaleIdAndCompanyId(Long saleId, Long companyId);
}
