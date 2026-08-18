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
    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByBrandIdAndActiveTrue(Long brandId, Pageable pageable);

    Page<Product> findBySupplierIdAndActiveTrue(Long supplierId, Pageable pageable);

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    // produtos sem giro
    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND p.id NOT IN (" +
            "SELECT DISTINCT si.product.id FROM SaleItem si " +
            "WHERE si.sale.date BETWEEN :start AND :end" +
            ")")
    List<Product> findProductsWithoutMovement(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}