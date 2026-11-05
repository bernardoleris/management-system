package br.com.system.services;

import br.com.system.data.dto.request.AdministratorCreateRequestDTO;
import br.com.system.data.dto.request.AdministratorRequestDTO;
import br.com.system.data.dto.request.ChangePasswordRequestDTO;
import br.com.system.data.dto.response.AdministratorResponseDTO;
import br.com.system.data.dto.response.UserEntityResponseDTO;
import br.com.system.enums.CompanyRole;
import br.com.system.exception.BusinessException;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Administrator;
import br.com.system.model.CompanyMembership;
import br.com.system.model.Permission;
import br.com.system.model.UserEntity;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.CompanyMembershipRepository;
import br.com.system.repository.CompanyRepository;
import br.com.system.repository.PermissionRepository;
import br.com.system.repository.UserEntityRepository;
import br.com.system.security.CurrentCompany;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class AdministratorServices {
    private final Logger logger = Logger.getLogger(AdministratorServices.class.getName());

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private CompanyMembershipRepository membershipRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CurrentCompany currentCompany;

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AdministratorResponseDTO> findAll() {
        logger.info("Finding all administrators!");

        return membershipRepository.findByCompanyIdOrderByIdAsc(currentCompany.getId()).stream()
                .map(CompanyMembership::getUser)
                .map(UserEntity::getId)
                .map(administratorRepository::findByUserId)
                .flatMap(java.util.Optional::stream)
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdministratorResponseDTO findById(Long id) {
        logger.info("Finding one administrator!");

        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public AdministratorResponseDTO create(AdministratorCreateRequestDTO dto) {
        logger.info("Creating one administrator!");

        if (userEntityRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already registered!");
        }

        if (administratorRepository.existsByLogin(dto.getLogin())) {
            throw new DuplicateResourceException("Login already registered!");
        }

        Permission permission = permissionRepository.findById(dto.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("No permission found for this ID!"));

        UserEntity user = new UserEntity();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPermissions(new ArrayList<>(List.of(permission)));
        userEntityRepository.save(user);

        Administrator entity = new Administrator();
        entity.setUser(user);
        entity.setLogin(dto.getLogin());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setLastLogin(null);

        Administrator savedAdministrator = administratorRepository.save(entity);
        createMembership(user, permission);
        return toResponseDTO(savedAdministrator);
    }

    @Transactional
    public AdministratorResponseDTO update(Long id, AdministratorRequestDTO dto) {
        logger.info("Updating one administrator!");

        Administrator entity = findEntityById(id);
        UserEntity user = entity.getUser();

        if (userEntityRepository.existsByEmailAndIdNot(dto.getEmail(), user.getId())) {
            throw new DuplicateResourceException("Email already registered!");
        }

        if (administratorRepository.existsByLoginAndIdNot(dto.getLogin(), id)) {
            throw new DuplicateResourceException("Login already registered!");
        }

        Permission permission = permissionRepository.findById(dto.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("No permission found for this ID!"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPermissions(new ArrayList<>(List.of(permission)));
        userEntityRepository.save(user);

        entity.setLogin(dto.getLogin());

        CompanyMembership membership = findMembership(user.getId());
        CompanyRole newRole = toCompanyRole(permission);
        if (membership.getRole() == CompanyRole.COMPANY_OWNER && newRole != CompanyRole.COMPANY_OWNER) {
            assertNotLastOwner(membership);
        }
        membership.setRole(newRole);
        membershipRepository.save(membership);

        return toResponseDTO(administratorRepository.save(entity));
    }

    @Transactional
    public void toggleActive(Long id) {
        logger.info("Toggling administrator active status!");

        Administrator entity = findEntityById(id);
        CompanyMembership membership = findMembership(entity.getUser().getId());
        if (Boolean.TRUE.equals(membership.getActive())) {
            assertNotLastOwner(membership);
        }
        membership.setActive(!Boolean.TRUE.equals(membership.getActive()));
        membershipRepository.save(membership);
    }

    private Administrator findEntityById(Long id) {
        Administrator administrator = administratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No administrator found for this ID!"));
        findMembership(administrator.getUser().getId());
        return administrator;
    }

    private void createMembership(UserEntity user, Permission permission) {
        CompanyMembership membership = new CompanyMembership();
        membership.setCompany(companyRepository.getReferenceById(currentCompany.getId()));
        membership.setUser(user);
        membership.setRole(toCompanyRole(permission));
        membership.setActive(true);
        membershipRepository.save(membership);
    }

    private CompanyMembership findMembership(Long userId) {
        return membershipRepository.findByCompanyIdAndUserId(currentCompany.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("No administrator found for this ID!"));
    }

    private CompanyRole toCompanyRole(Permission permission) {
        return switch (permission.getAuthority()) {
            case "ROLE_OWNER" -> CompanyRole.COMPANY_OWNER;
            case "ROLE_ADMIN" -> CompanyRole.ADMIN;
            default -> CompanyRole.OPERATOR;
        };
    }

    private void assertNotLastOwner(CompanyMembership membership) {
        if (membership.getRole() != CompanyRole.COMPANY_OWNER) {
            return;
        }
        long owners = membershipRepository.countByCompanyIdAndRoleAndActiveTrue(
                currentCompany.getId(), CompanyRole.COMPANY_OWNER);
        if (owners <= 1) {
            throw new BusinessException("A company must have at least one active owner!");
        }
    }

    private AdministratorResponseDTO toResponseDTO(Administrator entity) {
        AdministratorResponseDTO dto = new AdministratorResponseDTO();
        dto.setId(entity.getId());
        dto.setLogin(entity.getLogin());
        dto.setLastLogin(entity.getLastLogin());

        if (entity.getUser() != null) {
            dto.setUser(ObjectMapper.parseObject(entity.getUser(), UserEntityResponseDTO.class));
            dto.setPermissions(entity.getUser().getPermissions()
                    .stream()
                    .map(Permission::getAuthority)
                    .toList());
        }

        return dto;
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequestDTO dto) {
        logger.info("Changing administrator password!");

        Administrator entity = findEntityById(id);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), entity.getPassword())) {
            throw new BusinessException("Current password is incorrect!");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match!");
        }

        entity.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        administratorRepository.save(entity);
    }
}
