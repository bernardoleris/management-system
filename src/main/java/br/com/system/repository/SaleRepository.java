package br.com.system.repository;

import br.com.system.enums.SaleStatus;
import br.com.system.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    Page<Sale> findByStatus(SaleStatus status, Pageable pageable);

    Page<Sale> findByAdminId(Long adminId, Pageable pageable);

    Page<Sale> findByClientId(Long clientId, Pageable pageable);

    // vendas por mês
    @Query("SELECT FUNCTION('MONTH', s.date) as month, COUNT(s) as total " +
            "FROM Sale s WHERE s.date BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
            "GROUP BY FUNCTION('MONTH', s.date) ORDER BY month")
    List<Object[]> countSalesByMonth(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // faturamento por mês
    @Query("SELECT FUNCTION('MONTH', s.date) as month, SUM(s.totalValue) as revenue " +
            "FROM Sale s WHERE s.date BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
            "GROUP BY FUNCTION('MONTH', s.date) ORDER BY month")
    List<Object[]> revenueByMonth(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // forma de pagamento
    @Query("SELECT s.paymentMethod, COUNT(s) as total " +
            "FROM Sale s WHERE s.date BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
            "GROUP BY s.paymentMethod ORDER BY total DESC")
    List<Object[]> countByPaymentMethod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
