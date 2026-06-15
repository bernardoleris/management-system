package br.com.system.services;

import br.com.system.data.dto.request.StockMovementRequestDTO;
import br.com.system.data.dto.response.StockMovementResponseDTO;
import br.com.system.enums.MovementType;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.model.Administrator;
import br.com.system.model.Product;
import br.com.system.model.StockMovement;
import br.com.system.model.Supplier;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.ProductRepository;
import br.com.system.repository.StockMovementRepository;
import br.com.system.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class StockMovementServices {
    private final Logger logger = Logger.getLogger(StockMovementServices.class.getName());

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    public List<StockMovementResponseDTO> findAll() {
        logger.info("Finding stock movements!");

        return stockMovementRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<StockMovementResponseDTO> findByProduct(Long productId) {
        logger.info("Finding stock movements by product!");

        findProduct(productId);

        return stockMovementRepository.findByProductId(productId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<StockMovementResponseDTO> findByAdmin(Long adminId) {
        logger.info("Finding stock movements by administrator!");

        findAdministrator(adminId);

        return stockMovementRepository.findByAdminId(adminId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<StockMovementResponseDTO> findBySupplier(Long supplierId) {
        logger.info("Finding stock movements by supplier!");

        findSupplier(supplierId);

        return stockMovementRepository.findBySupplierId(supplierId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<StockMovementResponseDTO> findByType(MovementType type) {
        logger.info("Finding stock movements by type!");

        return stockMovementRepository.findByType(type).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public StockMovementResponseDTO findById(Long id) {
        logger.info("Finding one stock movement!");

        return toResponseDTO(findMovement(id));
    }

    public StockMovementResponseDTO create(StockMovementRequestDTO dto) {
        logger.info("Creating one stock movement!");

        Product product = findProduct(dto.getProductId());
        Administrator admin = findAdministrator(dto.getAdminId());
        Supplier supplier = dto.getSupplierId() == null ? null : findSupplier(dto.getSupplierId());

        StockMovement entity = new StockMovement();
        setMovementFields(entity, dto, product, admin, supplier);
        applyMovement(entity, false);

        return toResponseDTO(stockMovementRepository.save(entity));
    }

    public StockMovementResponseDTO update(Long id, StockMovementRequestDTO dto) {
        logger.info("Updating one stock movement!");

        StockMovement entity = findMovement(id);
        reverseMovement(entity);

        Product product = findProduct(dto.getProductId());
        Administrator admin = findAdministrator(dto.getAdminId());
        Supplier supplier = dto.getSupplierId() == null ? null : findSupplier(dto.getSupplierId());

        setMovementFields(entity, dto, product, admin, supplier);
        applyMovement(entity, false);

        return toResponseDTO(stockMovementRepository.save(entity));
    }

    public void delete(Long id) {
        logger.info("Deleting one stock movement!");

        StockMovement entity = findMovement(id);
        reverseMovement(entity);
        stockMovementRepository.delete(entity);
    }

    private void setMovementFields(
            StockMovement entity,
            StockMovementRequestDTO dto,
            Product product,
            Administrator admin,
            Supplier supplier) {
        entity.setProduct(product);
        entity.setAdmin(admin);
        entity.setSupplier(supplier);
        entity.setType(dto.getType());
        entity.setQuantity(dto.getQuantity());
        entity.setReason(dto.getReason());
    }

    private void applyMovement(StockMovement movement, boolean reverse) {
        Product product = movement.getProduct();
        int quantity = movement.getQuantity() == null ? 0 : movement.getQuantity();
        int delta = movement.getType() == MovementType.EXIT ? -quantity : quantity;

        if (reverse) {
            delta = -delta;
        }

        int updatedQuantity = product.getQuantity() + delta;
        if (updatedQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot become negative!");
        }

        product.setQuantity(updatedQuantity);
        productRepository.save(product);
    }

    private void reverseMovement(StockMovement movement) {
        applyMovement(movement, true);
    }

    private StockMovement findMovement(Long id) {
        return stockMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No stock movement found for this ID!"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No product found for this ID!"));
    }

    private Administrator findAdministrator(Long adminId) {
        return administratorRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("No administrator found for this ID!"));
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("No supplier found for this ID!"));
    }

    private StockMovementResponseDTO toResponseDTO(StockMovement entity) {
        StockMovementResponseDTO dto = new StockMovementResponseDTO();

        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setQuantity(entity.getQuantity());
        dto.setReason(entity.getReason());
        dto.setDate(entity.getDate());

        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getName());
        }

        if (entity.getAdmin() != null) {
            dto.setAdminId(entity.getAdmin().getId());
            dto.setAdminLogin(entity.getAdmin().getLogin());
        }

        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierTradeName(entity.getSupplier().getTradeName());
        }

        return dto;
    }
}
