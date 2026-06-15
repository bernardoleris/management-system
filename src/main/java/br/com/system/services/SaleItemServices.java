package br.com.system.services;

import br.com.system.data.dto.request.SaleItemRequestDTO;
import br.com.system.data.dto.response.SaleItemResponseDTO;
import br.com.system.enums.SaleStatus;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.model.Product;
import br.com.system.model.Sale;
import br.com.system.model.SaleItem;
import br.com.system.repository.ProductRepository;
import br.com.system.repository.SaleItemRepository;
import br.com.system.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

@Service
public class SaleItemServices {
    private final Logger logger = Logger.getLogger(SaleItemServices.class.getName());

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<SaleItemResponseDTO> findBySale(Long saleId) {
        logger.info("Finding sale items!");

        findSale(saleId);

        return saleItemRepository.findBySaleId(saleId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SaleItemResponseDTO findById(Long saleId, Long itemId) {
        logger.info("Finding one sale item!");

        return toResponseDTO(findSaleItem(saleId, itemId));
    }

    public SaleItemResponseDTO create(Long saleId, SaleItemRequestDTO dto) {
        logger.info("Creating one sale item!");

        Sale sale = findSale(saleId);
        SaleItem item = buildSaleItem(sale, dto);

        sale.getItems().add(item);
        decreaseStockIfCompleted(sale, item);
        recalculateTotal(sale);
        saleRepository.save(sale);

        return toResponseDTO(item);
    }

    public SaleItemResponseDTO update(Long saleId, Long itemId, SaleItemRequestDTO dto) {
        logger.info("Updating one sale item!");

        SaleItem item = findSaleItem(saleId, itemId);
        Sale sale = item.getSale();

        restoreStockIfCompleted(sale, item);
        setSaleItemFields(item, dto);
        decreaseStockIfCompleted(sale, item);
        recalculateTotal(sale);
        saleRepository.save(sale);

        return toResponseDTO(item);
    }

    public void delete(Long saleId, Long itemId) {
        logger.info("Deleting one sale item!");

        SaleItem item = findSaleItem(saleId, itemId);
        Sale sale = item.getSale();

        restoreStockIfCompleted(sale, item);
        sale.getItems().remove(item);
        recalculateTotal(sale);
        saleRepository.save(sale);
    }

    private SaleItem buildSaleItem(Sale sale, SaleItemRequestDTO dto) {
        SaleItem item = new SaleItem();
        item.setSale(sale);
        setSaleItemFields(item, dto);

        return item;
    }

    private void setSaleItemFields(SaleItem item, SaleItemRequestDTO dto) {
        Product product = findProduct(dto.getProductId());
        BigDecimal unitPrice = dto.getUnitPrice() == null ? product.getSalePrice() : dto.getUnitPrice();
        BigDecimal discount = valueOrZero(dto.getDiscount());
        Integer quantity = dto.getQuantity() == null ? 0 : dto.getQuantity();

        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setDiscount(discount);
        item.setSubtotal(calculateSubtotal(quantity, unitPrice, discount));
    }

    private void recalculateTotal(Sale sale) {
        BigDecimal itemsTotal = sale.getItems().stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sale.setTotalValue(itemsTotal.subtract(valueOrZero(sale.getDiscount())));
    }

    private void decreaseStockIfCompleted(Sale sale, SaleItem item) {
        if (sale.getStatus() != SaleStatus.COMPLETED) {
            return;
        }

        Product product = item.getProduct();
        product.setQuantity(product.getQuantity() - item.getQuantity());
        productRepository.save(product);
    }

    private void restoreStockIfCompleted(Sale sale, SaleItem item) {
        if (sale.getStatus() != SaleStatus.COMPLETED) {
            return;
        }

        Product product = item.getProduct();
        product.setQuantity(product.getQuantity() + item.getQuantity());
        productRepository.save(product);
    }

    private BigDecimal calculateSubtotal(Integer quantity, BigDecimal unitPrice, BigDecimal discount) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(discount);
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Sale findSale(Long saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("No sale found for this ID!"));
    }

    private SaleItem findSaleItem(Long saleId, Long itemId) {
        SaleItem item = saleItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("No sale item found for this ID!"));

        if (!item.getSale().getId().equals(saleId)) {
            throw new ResourceNotFoundException("No sale item found for this sale!");
        }

        return item;
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No product found for this ID!"));
    }

    private SaleItemResponseDTO toResponseDTO(SaleItem entity) {
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
