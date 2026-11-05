package br.com.system.repository;

import br.com.system.model.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findBySaleIdAndSaleCompanyId(Long saleId, Long companyId);

    Optional<SaleItem> findByIdAndSaleIdAndSaleCompanyId(Long id, Long saleId, Long companyId);
    List<SaleItem> findBySaleId(Long saleId);

    boolean existsByProductId(Long productId);

    // produtos mais vendidos
    @Query("SELECT si.product.id, si.product.name, SUM(si.quantity) as total " +
            "FROM SaleItem si WHERE si.sale.company.id = :companyId AND si.sale.date BETWEEN :start AND :end " +
            "AND si.sale.status = 'COMPLETED' " +
            "GROUP BY si.product.id, si.product.name ORDER BY total DESC")
    List<Object[]> findTopProducts(
            @Param("companyId") Long companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
}
