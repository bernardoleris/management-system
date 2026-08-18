package br.com.system.repository;

import br.com.system.model.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findBySaleId(Long saleId);

    boolean existsByProductId(Long productId);

    // produtos mais vendidos
    @Query("SELECT si.product.id, si.product.name, SUM(si.quantity) as total " +
            "FROM SaleItem si WHERE si.sale.date BETWEEN :start AND :end " +
            "AND si.sale.status = 'COMPLETED' " +
            "GROUP BY si.product.id, si.product.name ORDER BY total DESC")
    List<Object[]> findTopProducts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
}
