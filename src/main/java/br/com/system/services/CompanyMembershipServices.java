package br.com.system.services;

import br.com.system.data.dto.request.CompanyMemberCreateRequestDTO;
import br.com.system.data.dto.request.CompanyMemberRoleRequestDTO;
import br.com.system.data.dto.request.ExistingCompanyMemberRequestDTO;
import br.com.system.data.dto.response.CompanyMemberResponseDTO;
import br.com.system.enums.CompanyRole;
import br.com.system.exception.BusinessException;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.model.Administrator;
import br.com.system.model.CompanyMembership;
import br.com.system.model.UserEntity;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.CompanyMembershipRepository;
import br.com.system.repository.CompanyRepository;
import br.com.system.repository.UserEntityRepository;
import br.com.system.security.CurrentCompany;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyMembershipServices {
    @Autowired
    private CompanyMembershipRepository membershipRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserEntityRepository userRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CurrentCompany currentCompany;

    @Transactional(readOnly = true)
    public List<CompanyMemberResponseDTO> findAll() {
        return membershipRepository.findByCompanyIdOrderByIdAsc(currentCompany.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CompanyMemberResponseDTO create(CompanyMemberCreateRequestDTO dto) {
        if (administratorRepository.existsByLogin(dto.getLogin())) {
            throw new DuplicateResourceException("Login already registered!");
        }

        UserEntity user = new UserEntity();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPermissions(new ArrayList<>());
        userRepository.save(user);

        Administrator administrator = new Administrator();
        administrator.setUser(user);
        administrator.setLogin(dto.getLogin());
        administrator.setPassword(passwordEncoder.encode(dto.getPassword()));
        administratorRepository.save(administrator);

        CompanyMembership membership = new CompanyMembership();
        membership.setCompany(companyRepository.getReferenceById(currentCompany.getId()));
        membership.setUser(user);
        membership.setRole(dto.getRole());
        membership.setActive(true);

        return toResponse(membershipRepository.save(membership));
    }

    @Transactional
    public CompanyMemberResponseDTO addExisting(ExistingCompanyMemberRequestDTO dto) {
        Administrator administrator = administratorRepository.findByLogin(dto.getLogin())
                .orElseThrow(() -> new ResourceNotFoundException("No administrator found for this login!"));
        Long companyId = currentCompany.getId();

        CompanyMembership membership = membershipRepository.findByCompanyIdAndUserId(companyId, administrator.getUser().getId())
                .orElseGet(CompanyMembership::new);

        if (membership.getId() != null && Boolean.TRUE.equals(membership.getActive())) {
            throw new DuplicateResourceException("This user is already a member of the company!");
        }

        membership.setCompany(companyRepository.getReferenceById(companyId));
        membership.setUser(administrator.getUser());
        membership.setRole(dto.getRole());
        membership.setActive(true);
        return toResponse(membershipRepository.save(membership));
    }

    @Transactional
    public CompanyMemberResponseDTO updateRole(Long membershipId, CompanyMemberRoleRequestDTO dto) {
        CompanyMembership membership = findMembership(membershipId);

        if (membership.getRole() == CompanyRole.COMPANY_OWNER
                && dto.getRole() != CompanyRole.COMPANY_OWNER) {
            assertNotLastOwner(membership);
        }

        membership.setRole(dto.getRole());
        return toResponse(membershipRepository.save(membership));
    }

    @Transactional
    public void deactivate(Long membershipId) {
        CompanyMembership membership = findMembership(membershipId);
        if (membership.getRole() == CompanyRole.COMPANY_OWNER && Boolean.TRUE.equals(membership.getActive())) {
            assertNotLastOwner(membership);
        }

        membership.setActive(false);
        membershipRepository.save(membership);
    }

    private CompanyMembership findMembership(Long membershipId) {
        return membershipRepository.findByIdAndCompanyId(membershipId, currentCompany.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No company member found for this ID!"));
    }

    private void assertNotLastOwner(CompanyMembership membership) {
        long owners = membershipRepository.countByCompanyIdAndRoleAndActiveTrue(
                currentCompany.getId(), CompanyRole.COMPANY_OWNER);
        if (owners <= 1) {
            throw new BusinessException("A company must have at least one active owner!");
        }
    }

    private CompanyMemberResponseDTO toResponse(CompanyMembership membership) {
        UserEntity user = membership.getUser();
        Administrator administrator = administratorRepository.findByUserId(user.getId()).orElse(null);

        CompanyMemberResponseDTO response = new CompanyMemberResponseDTO();
        response.setMembershipId(membership.getId());
        response.setUserId(user.getId());
        response.setAdministratorId(administrator == null ? null : administrator.getId());
        response.setLogin(administrator == null ? null : administrator.getLogin());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(membership.getRole());
        response.setActive(membership.getActive());
        return response;
    }
}
