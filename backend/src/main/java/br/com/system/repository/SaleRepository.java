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
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    Page<Sale> findByCompanyId(Long companyId, Pageable pageable);

    Page<Sale> findByCompanyIdAndStatus(Long companyId, SaleStatus status, Pageable pageable);

    Page<Sale> findByCompanyIdAndAdminId(Long companyId, Long adminId, Pageable pageable);

    Page<Sale> findByCompanyIdAndClientId(Long companyId, Long clientId, Pageable pageable);

    Optional<Sale> findByIdAndCompanyId(Long id, Long companyId);

    // Daily sales buckets include the full calendar date so ranges crossing years remain distinct.
    @Query("SELECT FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date), FUNCTION('DAY', s.date), COUNT(s) " +
            "FROM Sale s WHERE s.company.id = :companyId AND s.date BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
            "GROUP BY FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date), FUNCTION('DAY', s.date) " +
            "ORDER BY FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date), FUNCTION('DAY', s.date)")
    List<Object[]> countSalesByDay(
            @Param("companyId") Long companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Monthly sales buckets include year and month to avoid merging the same month across years.
    @Query("SELECT FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date), COUNT(s) " +
            "FROM Sale s WHERE s.company.id = :companyId AND s.date BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
            "GROUP BY FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date) " +
            "ORDER BY FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date)")
    List<Object[]> countSalesByMonth(
            @Param("companyId") Long companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date), FUNCTION('DAY', s.date), SUM(s.totalValue) " +
            "FROM Sale s WHERE s.company.id = :companyId AND s.date BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
            "GROUP BY FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date), FUNCTION('DAY', s.date) " +
            "ORDER BY FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date), FUNCTION('DAY', s.date)")
    List<Object[]> revenueByDay(
            @Param("companyId") Long companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date), SUM(s.totalValue) " +
            "FROM Sale s WHERE s.company.id = :companyId AND s.date BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
            "GROUP BY FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date) " +
            "ORDER BY FUNCTION('YEAR', s.date), FUNCTION('MONTH', s.date)")
    List<Object[]> revenueByMonth(
            @Param("companyId") Long companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // forma de pagamento
    @Query("SELECT s.paymentMethod, COUNT(s) as total " +
            "FROM Sale s WHERE s.company.id = :companyId AND s.date BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
            "GROUP BY s.paymentMethod ORDER BY total DESC")
    List<Object[]> countByPaymentMethod(
            @Param("companyId") Long companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
