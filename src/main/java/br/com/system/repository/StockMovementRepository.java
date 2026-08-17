package br.com.system.repository;

import br.com.system.enums.MovementType;
import br.com.system.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByAdminId(Long adminId, Pageable pageable);
    Page<StockMovement> findBySupplierId(Long supplierId, Pageable pageable);
    Page<StockMovement> findByType(MovementType type, Pageable pageable);
}
