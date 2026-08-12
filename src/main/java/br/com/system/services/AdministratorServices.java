package br.com.system.services;

import br.com.system.data.dto.request.AdministratorRequestDTO;
import br.com.system.data.dto.response.AdministratorResponseDTO;
import br.com.system.exception.BusinessException;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.model.Administrator;
import br.com.system.model.Permission;
import br.com.system.model.UserEntity;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.PermissionRepository;
import br.com.system.repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@Service
public class AdministratorServices {
    private final Logger logger = Logger.getLogger(AdministratorServices.class.getName());

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AdministratorResponseDTO> findAll() {
        logger.info("Finding all administrators!");

        return administratorRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdministratorResponseDTO findById(Long id) {
        logger.info("Finding one administrator!");

        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public AdministratorResponseDTO create(AdministratorRequestDTO dto) {
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
        user.setPermissions(List.of(permission));
        userEntityRepository.save(user);

        Administrator entity = new Administrator();
        entity.setUser(user);
        entity.setLogin(dto.getLogin());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setLastLogin(null);

        return toResponseDTO(administratorRepository.save(entity));
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
        user.setPermissions(List.of(permission));
        userEntityRepository.save(user);

        entity.setLogin(dto.getLogin());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));

        return toResponseDTO(administratorRepository.save(entity));
    }

    @Transactional
    public void toggleActive(Long id) {
        logger.info("Toggling administrator active status!");

        Administrator entity = findEntityById(id);
        UserEntity user = entity.getUser();
        user.setActive(!user.getActive());
        userEntityRepository.save(user);
    }

    private Administrator findEntityById(Long id) {
        return administratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No administrator found for this ID!"));
    }

    private AdministratorResponseDTO toResponseDTO(Administrator entity) {
        AdministratorResponseDTO dto = new AdministratorResponseDTO();
        dto.setId(entity.getId());
        dto.setLogin(entity.getLogin());
        dto.setLastLogin(entity.getLastLogin());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setFirstName(entity.getUser().getFirstName());
            dto.setLastName(entity.getUser().getLastName());
            dto.setEmail(entity.getUser().getEmail());
            dto.setPhone(entity.getUser().getPhone());
            dto.setActive(entity.getUser().getActive());
            dto.setPermissions(entity.getUser().getPermissions()
                    .stream()
                    .map(Permission::getAuthority)
                    .toList());
        }

        return dto;
    }
}