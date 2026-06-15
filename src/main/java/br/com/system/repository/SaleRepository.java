package br.com.system.repository;

import br.com.system.enums.SaleStatus;
import br.com.system.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByStatus(SaleStatus status);

    List<Sale> findByAdminId(Long adminId);

    List<Sale> findByClientId(Long clientId);
}
