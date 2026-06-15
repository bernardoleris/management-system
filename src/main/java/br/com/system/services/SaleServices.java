package br.com.system.services;

import br.com.system.data.dto.request.SaleItemRequestDTO;
import br.com.system.data.dto.request.SaleRequestDTO;
import br.com.system.data.dto.response.SaleItemResponseDTO;
import br.com.system.data.dto.response.SaleResponseDTO;
import br.com.system.enums.SaleStatus;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.model.Administrator;
import br.com.system.model.Client;
import br.com.system.model.Product;
import br.com.system.model.Sale;
import br.com.system.model.SaleItem;
import br.com.system.repository.AdministratorRepository;
import br.com.system.repository.ClientRepository;
import br.com.system.repository.ProductRepository;
import br.com.system.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class SaleServices {
    private final Logger logger = Logger.getLogger(SaleServices.class.getName());

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<SaleResponseDTO> findAll() {
        logger.info("Finding sales!");

        return saleRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<SaleResponseDTO> findByStatus(SaleStatus status) {
        logger.info("Finding sales by status!");

        return saleRepository.findByStatus(status).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<SaleResponseDTO> findByAdmin(Long adminId) {
        logger.info("Finding sales by administrator!");

        findAdministrator(adminId);

        return saleRepository.findByAdminId(adminId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<SaleResponseDTO> findByClient(Long clientId) {
        logger.info("Finding sales by client!");

        findClient(clientId);

        return saleRepository.findByClientId(clientId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SaleResponseDTO findById(Long id) {
        logger.info("Finding one sale!");

        return toResponseDTO(findSale(id));
    }

    public SaleResponseDTO create(SaleRequestDTO sale) {
        logger.info("Creating one sale!");

        Sale entity = new Sale();
        setSaleFields(entity, sale);

        return toResponseDTO(saleRepository.save(entity));
    }

    public SaleResponseDTO update(Long id, SaleRequestDTO sale) {
        logger.info("Updating one sale!");

        Sale entity = findSale(id);
        restoreStockIfCompleted(entity);
        setSaleFields(entity, sale);

        return toResponseDTO(saleRepository.save(entity));
    }

    public SaleResponseDTO cancel(Long id) {
        logger.info("Canceling one sale!");

        Sale entity = findSale(id);
        restoreStockIfCompleted(entity);
        entity.setStatus(SaleStatus.CANCELED);

        return toResponseDTO(saleRepository.save(entity));
    }

    public void delete(Long id) {
        logger.info("Deleting one sale!");

        Sale entity = findSale(id);
        restoreStockIfCompleted(entity);
        saleRepository.delete(entity);
    }

    private void setSaleFields(Sale entity, SaleRequestDTO sale) {
        Administrator admin = findAdministrator(sale.getAdminId());
        Client client = findClient(sale.getClientId());

        entity.setAdmin(admin);
        entity.setClient(client);
        entity.setStatus(sale.getStatus() == null ? SaleStatus.PENDING : sale.getStatus());
        entity.setPaymentMethod(sale.getPaymentMethod());
        entity.setDiscount(valueOrZero(sale.getDiscount()));
        entity.setNotes(sale.getNotes());

        entity.getItems().clear();

        if (sale.getItems() != null) {
            for (SaleItemRequestDTO itemDTO : sale.getItems()) {
                SaleItem item = buildSaleItem(entity, itemDTO);
                entity.getItems().add(item);
            }
        }

        recalculateTotal(entity);
        decreaseStockIfCompleted(entity);
    }

    private SaleItem buildSaleItem(Sale sale, SaleItemRequestDTO itemDTO) {
        Product product = findProduct(itemDTO.getProductId());
        BigDecimal unitPrice = itemDTO.getUnitPrice() == null ? product.getSalePrice() : itemDTO.getUnitPrice();
        BigDecimal discount = valueOrZero(itemDTO.getDiscount());
        Integer quantity = itemDTO.getQuantity() == null ? 0 : itemDTO.getQuantity();

        SaleItem item = new SaleItem();
        item.setSale(sale);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setDiscount(discount);
        item.setSubtotal(calculateSubtotal(quantity, unitPrice, discount));

        return item;
    }

    private void recalculateTotal(Sale sale) {
        BigDecimal itemsTotal = sale.getItems().stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sale.setTotalValue(itemsTotal.subtract(valueOrZero(sale.getDiscount())));
    }

    private void decreaseStockIfCompleted(Sale sale) {
        if (sale.getStatus() != SaleStatus.COMPLETED) {
            return;
        }

        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
        }
    }

    private void restoreStockIfCompleted(Sale sale) {
        if (sale.getStatus() != SaleStatus.COMPLETED) {
            return;
        }

        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private BigDecimal calculateSubtotal(Integer quantity, BigDecimal unitPrice, BigDecimal discount) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(discount);
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Sale findSale(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No sale found for this ID!"));
    }

    private Administrator findAdministrator(Long adminId) {
        return administratorRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("No administrator found for this ID!"));
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("No client found for this ID!"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No product found for this ID!"));
    }

    private SaleResponseDTO toResponseDTO(Sale entity) {
        SaleResponseDTO dto = new SaleResponseDTO();

        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setStatus(entity.getStatus());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setDiscount(entity.getDiscount());
        dto.setTotalValue(entity.getTotalValue());
        dto.setNotes(entity.getNotes());

        if (entity.getAdmin() != null) {
            dto.setAdminId(entity.getAdmin().getId());
            dto.setAdminLogin(entity.getAdmin().getLogin());
        }

        if (entity.getClient() != null) {
            dto.setClientId(entity.getClient().getId());
            dto.setClientDocumentNumber(entity.getClient().getDocumentNumber());
        }

        List<SaleItemResponseDTO> items = new ArrayList<>();
        for (SaleItem item : entity.getItems()) {
            items.add(toItemResponseDTO(item));
        }
        dto.setItems(items);

        return dto;
    }

    private SaleItemResponseDTO toItemResponseDTO(SaleItem entity) {
        SaleItemResponseDTO dto = new SaleItemResponseDTO();

        dto.setId(entity.getId());
        dto.setQuantity(entity.getQuantity());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setSubtotal(entity.getSubtotal());
        dto.setDiscount(entity.getDiscount());

        if (entity.getSale() != null) {
            dto.setSaleId(entity.getSale().getId());
        }

        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getName());
        }

        return dto;
    }
}
