package br.com.system.repository;

import br.com.system.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCompanyId(Long companyId, Pageable pageable);

    Page<Product> findByCompanyIdAndActiveTrue(Long companyId, Pageable pageable);

    Page<Product> findByCompanyIdAndCategoryIdAndActiveTrue(Long companyId, Long categoryId, Pageable pageable);

    Page<Product> findByCompanyIdAndBrandIdAndActiveTrue(Long companyId, Long brandId, Pageable pageable);

    Page<Product> findByCompanyIdAndSupplierIdAndActiveTrue(Long companyId, Long supplierId, Pageable pageable);

    Optional<Product> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Product> findByCompanyIdAndBarcode(Long companyId, String barcode);

    boolean existsByCompanyIdAndBarcode(Long companyId, String barcode);

    boolean existsByCompanyIdAndBarcodeAndIdNot(Long companyId, String barcode, Long id);

    // produtos sem giro
    @Query("SELECT p FROM Product p WHERE p.company.id = :companyId AND p.active = true " +
            "AND p.id NOT IN (" +
            "SELECT DISTINCT si.product.id FROM SaleItem si " +
            "WHERE si.sale.company.id = :companyId AND si.sale.date BETWEEN :start AND :end" +
            ")")
    List<Product> findProductsWithoutMovement(
            @Param("companyId") Long companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
