package br.com.system.repository;

import br.com.system.model.StockMovementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementItemRepository extends JpaRepository<StockMovementItem, Long> {}