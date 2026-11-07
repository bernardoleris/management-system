package br.com.system.repository;

import br.com.system.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByIdAndCompanyId(Long id, Long companyId);
}
