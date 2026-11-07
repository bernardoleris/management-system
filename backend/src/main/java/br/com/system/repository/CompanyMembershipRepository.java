package br.com.system.repository;

import br.com.system.model.CompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {
    List<CompanyMembership> findByCompanyIdOrderByIdAsc(Long companyId);

    List<CompanyMembership> findByUserIdAndActiveTrueOrderByIdAsc(Long userId);

    Optional<CompanyMembership> findByIdAndCompanyId(Long id, Long companyId);

    Optional<CompanyMembership> findByCompanyIdAndUserIdAndActiveTrue(Long companyId, Long userId);

    Optional<CompanyMembership> findByCompanyIdAndUserId(Long companyId, Long userId);

    boolean existsByCompanyIdAndUserIdAndActiveTrue(Long companyId, Long userId);

    Optional<CompanyMembership> findByIdAndUserIdAndCompanyIdAndActiveTrue(
            Long id, Long userId, Long companyId);

    long countByCompanyIdAndRoleAndActiveTrue(Long companyId, br.com.system.enums.CompanyRole role);
}
