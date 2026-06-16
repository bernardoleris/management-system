package br.com.system.repository;

import br.com.system.enums.MovementType;
import br.com.system.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByAdminId(Long adminId);
    List<StockMovement> findBySupplierId(Long supplierId);
    List<StockMovement> findByType(MovementType type);
}