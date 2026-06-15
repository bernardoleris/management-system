package br.com.system.repository;

import br.com.system.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByActiveTrue();

    List<Alert> findByReadFalseAndActiveTrue();

    List<Alert> findByAdminIdAndActiveTrue(Long adminId);

    List<Alert> findByProductIdAndActiveTrue(Long productId);
}
