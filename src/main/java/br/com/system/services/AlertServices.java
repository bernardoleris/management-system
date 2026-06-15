package br.com.system.services;

import br.com.system.data.dto.request.AlertRequestDTO;
import br.com.system.data.dto.response.AlertResponseDTO;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.model.Administrator;
import br.com.system.model.Alert;
import br.com.system.model.Product;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.AlertRepository;
import br.com.system.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class AlertServices {
    private final Logger logger = Logger.getLogger(AlertServices.class.getName());

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    public List<AlertResponseDTO> findAll() {
        logger.info("Finding alerts!");

        return alertRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<AlertResponseDTO> findActive() {
        logger.info("Finding active alerts!");

        return alertRepository.findByActiveTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<AlertResponseDTO> findUnread() {
        logger.info("Finding unread alerts!");

        return alertRepository.findByReadFalseAndActiveTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<AlertResponseDTO> findByAdmin(Long adminId) {
        logger.info("Finding alerts by administrator!");

        findAdministrator(adminId);

        return alertRepository.findByAdminIdAndActiveTrue(adminId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<AlertResponseDTO> findByProduct(Long productId) {
        logger.info("Finding alerts by product!");

        findProduct(productId);

        return alertRepository.findByProductIdAndActiveTrue(productId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public AlertResponseDTO findById(Long id) {
        logger.info("Finding one alert!");

        return toResponseDTO(findAlert(id));
    }

    public AlertResponseDTO create(AlertRequestDTO dto) {
        logger.info("Creating one alert!");

        Alert entity = new Alert();
        setAlertFields(entity, dto);

        return toResponseDTO(alertRepository.save(entity));
    }

    public AlertResponseDTO update(Long id, AlertRequestDTO dto) {
        logger.info("Updating one alert!");

        Alert entity = findAlert(id);
        setAlertFields(entity, dto);

        return toResponseDTO(alertRepository.save(entity));
    }

    public AlertResponseDTO markAsRead(Long id) {
        logger.info("Marking one alert as read!");

        Alert entity = findAlert(id);
        entity.setRead(true);
        entity.setReadAt(LocalDateTime.now());

        return toResponseDTO(alertRepository.save(entity));
    }

    public void delete(Long id) {
        logger.info("Deleting one alert!");

        Alert entity = findAlert(id);
        entity.setActive(false);
        alertRepository.save(entity);
    }

    private void setAlertFields(Alert entity, AlertRequestDTO dto) {
        Product product = findProduct(dto.getProductId());
        Administrator admin = findAdministrator(dto.getAdminId());

        entity.setProduct(product);
        entity.setAdmin(admin);
        entity.setType(dto.getType());
        entity.setMinimumQuantity(dto.getMinimumQuantity());
        entity.setMessage(dto.getMessage());

        if (dto.getRead() != null) {
            entity.setRead(dto.getRead());
            entity.setReadAt(Boolean.TRUE.equals(dto.getRead()) ? LocalDateTime.now() : null);
        }

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }

    private Alert findAlert(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No alert found for this ID!"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No product found for this ID!"));
    }

    private Administrator findAdministrator(Long adminId) {
        return administratorRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("No administrator found for this ID!"));
    }

    private AlertResponseDTO toResponseDTO(Alert entity) {
        AlertResponseDTO dto = new AlertResponseDTO();

        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setMinimumQuantity(entity.getMinimumQuantity());
        dto.setMessage(entity.getMessage());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setReadAt(entity.getReadAt());
        dto.setRead(entity.getRead());
        dto.setActive(entity.getActive());

        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getName());
        }

        if (entity.getAdmin() != null) {
            dto.setAdminId(entity.getAdmin().getId());
            dto.setAdminLogin(entity.getAdmin().getLogin());
        }

        return dto;
    }
}
