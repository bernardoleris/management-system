package br.com.system.services;

import br.com.system.data.dto.request.ClientRequestDTO;
import br.com.system.data.dto.response.AddressResponseDTO;
import br.com.system.data.dto.response.ClientResponseDTO;
import br.com.system.data.dto.response.UserEntityResponseDTO;
import br.com.system.enums.DocumentType;
import br.com.system.exception.BusinessException;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Address;
import br.com.system.model.Client;
import br.com.system.model.UserEntity;
import br.com.system.repository.AddressRepository;
import br.com.system.repository.ClientRepository;
import br.com.system.repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.logging.Logger;

@Service
public class ClientServices {
    private final Logger logger = Logger.getLogger(ClientServices.class.getName());

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserEntityRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> findAll(Pageable pageable) {
        logger.info("Finding clients!");

        return clientRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO findById(Long id) {
        logger.info("Finding one client!");

        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public ClientResponseDTO create(ClientRequestDTO dto) {
        logger.info("Creating one client!");

        validateDocumentNumber(dto);

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already registered!");
        }

        if (clientRepository.existsByDocumentNumber(dto.getDocumentNumber())) {
            throw new DuplicateResourceException("Document number already registered!");
        }

        // cria o UserEntity
        UserEntity user = new UserEntity();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        userRepository.save(user);

        // cria o endereço se foi informado
        Address address = null;
        if (dto.getAddress() != null) {
            address = new Address();
            setAddressFields(address, dto);
            addressRepository.save(address);
        }

        // cria o client
        Client entity = new Client();
        entity.setUser(user);
        entity.setDocumentType(dto.getDocumentType());
        entity.setDocumentNumber(dto.getDocumentNumber());
        entity.setBirthDate(dto.getBirthDate());
        entity.setAddress(address);

        return toResponseDTO(clientRepository.save(entity));
    }

    @Transactional
    public ClientResponseDTO update(Long id, ClientRequestDTO dto) {
        logger.info("Updating one client!");

        Client entity = findEntityById(id);
        UserEntity user = entity.getUser();

        validateDocumentNumber(dto);

        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), user.getId())) {
            throw new DuplicateResourceException("Email already registered!");
        }

        if (clientRepository.existsByDocumentNumberAndIdNot(dto.getDocumentNumber(), id)) {
            throw new DuplicateResourceException("Document number already registered!");
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        userRepository.save(user);

        entity.setDocumentType(dto.getDocumentType());
        entity.setDocumentNumber(dto.getDocumentNumber());
        entity.setBirthDate(dto.getBirthDate());

        return toResponseDTO(clientRepository.save(entity));
    }

    @Transactional
    public void toggleActive(Long id) {
        logger.info("Toggling client active status!");

        Client entity = findEntityById(id);
        UserEntity user = entity.getUser();
        user.setActive(!user.getActive());
        userRepository.save(user);
    }

    // ─── Métodos internos ─────────────────────────────────────────────────────

    private void validateDocumentNumber(ClientRequestDTO dto) {
        if (dto.getDocumentType() == DocumentType.CPF &&
                dto.getDocumentNumber().length() != 11) {
            throw new BusinessException("CPF must have exactly 11 digits!");
        }

        if (dto.getDocumentType() == DocumentType.CNPJ &&
                dto.getDocumentNumber().length() != 14) {
            throw new BusinessException("CNPJ must have exactly 14 digits!");
        }
    }

    private void setAddressFields(Address address, ClientRequestDTO dto) {
        address.setStreet(dto.getAddress().getStreet());
        address.setNumber(dto.getAddress().getNumber());
        address.setComplement(dto.getAddress().getComplement());
        address.setNeighborhood(dto.getAddress().getNeighborhood());
        address.setCity(dto.getAddress().getCity());
        address.setState(dto.getAddress().getState());
        address.setZipCode(dto.getAddress().getZipCode());
    }

    private Client findEntityById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No client found for this ID!"));
    }

    private ClientResponseDTO toResponseDTO(Client entity) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(entity.getId());
        dto.setDocumentType(entity.getDocumentType());
        dto.setDocumentNumber(entity.getDocumentNumber());
        dto.setBirthDate(entity.getBirthDate());

        if (entity.getUser() != null) {
            dto.setUser(ObjectMapper.parseObject(entity.getUser(), UserEntityResponseDTO.class));
        }

        if (entity.getAddress() != null) {
            dto.setAddress(ObjectMapper.parseObject(entity.getAddress(), AddressResponseDTO.class));
        }

        return dto;
    }
}
