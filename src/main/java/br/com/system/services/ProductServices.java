package br.com.system.services;

import br.com.system.data.dto.request.ProductRequestDTO;
import br.com.system.data.dto.response.ProductResponseDTO;
import br.com.system.exception.BusinessException;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.model.Brand;
import br.com.system.model.Category;
import br.com.system.model.Product;
import br.com.system.model.Supplier;
import br.com.system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

@Service
public class ProductServices {
    private final Logger logger = Logger.getLogger(ProductServices.class.getName());

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private StockMovementItemRepository stockMovementItemRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        logger.info("Finding products!");

        return productRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findActive(Pageable pageable) {
        logger.info("Finding active products!");

        return productRepository.findByActiveTrue(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findByCategory(Long categoryId, Pageable pageable) {
        logger.info("Finding products by category!");

        findCategory(categoryId);

        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findByBrand(Long brandId, Pageable pageable) {
        logger.info("Finding products by brand!");

        findBrand(brandId);

        return productRepository.findByBrandIdAndActiveTrue(brandId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findBySupplier(Long supplierId, Pageable pageable) {
        logger.info("Finding products by supplier!");

        findSupplier(supplierId);

        return productRepository.findBySupplierIdAndActiveTrue(supplierId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {
        logger.info("Finding one product!");

        return toResponseDTO(findProduct(id));
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findByBarcode(String barcode) {
        logger.info("Finding one product by barcode!");

        Product entity = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("No product found for this barcode!"));

        return toResponseDTO(entity);
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO product) {
        logger.info("Creating one product!");

        if (product.getBarcode() != null && productRepository.existsByBarcode(product.getBarcode())) {
            throw new DuplicateResourceException("Barcode already registered!");
        }

        Product entity = new Product();
        setProductFields(entity, product);

        return toResponseDTO(productRepository.save(entity));
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductRequestDTO product) {
        logger.info("Updating one product!");

        Product entity = findProduct(id);

        if (product.getBarcode() != null &&
                productRepository.existsByBarcodeAndIdNot(product.getBarcode(), id)) {
            throw new DuplicateResourceException("Barcode already registered!");
        }

        setProductFields(entity, product);

        return toResponseDTO(productRepository.save(entity));
    }

    @Transactional
    public void toggleActive(Long id) {
        logger.info("Toggling product active status!");

        Product entity = findProduct(id);
        entity.setActive(!entity.getActive());
        productRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Deleting one product!");

        Product entity = findProduct(id);

        if (saleItemRepository.existsByProductId(id)) {
            throw new BusinessException("Product cannot be deleted because it has sales records!");
        }

        if (stockMovementItemRepository.existsByProductId(id)) {
            throw new BusinessException("Product cannot be deleted because it has stock movement records!");
        }

        if (alertRepository.existsByProductId(id)) {
            throw new BusinessException("Product cannot be deleted because it has alerts!");
        }

        productRepository.delete(entity);
    }

    // ─── Métodos internos ─────────────────────────────────────────────────────

    private void setProductFields(Product entity, ProductRequestDTO product) {
        Category category = findCategory(product.getCategoryId());
        Brand brand = findBrand(product.getBrandId());
        Supplier supplier = product.getSupplierId() != null
                ? findSupplier(product.getSupplierId())
                : null;

        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setBarcode(product.getBarcode());
        entity.setPurchasePrice(product.getPurchasePrice());
        entity.setSalePrice(product.getSalePrice());
        entity.setQuantity(product.getQuantity());
        entity.setCategory(category);
        entity.setBrand(brand);
        entity.setSupplier(supplier);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No product found for this ID!"));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No category found for this ID!"));
    }

    private Brand findBrand(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("No brand found for this ID!"));
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("No supplier found for this ID!"));
    }

    private ProductResponseDTO toResponseDTO(Product entity) {
        ProductResponseDTO dto = new ProductResponseDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setBarcode(entity.getBarcode());
        dto.setPurchasePrice(entity.getPurchasePrice());
        dto.setSalePrice(entity.getSalePrice());
        dto.setQuantity(entity.getQuantity());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setActive(entity.getActive());

        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
            dto.setCategoryName(entity.getCategory().getName());
        }

        if (entity.getBrand() != null) {
            dto.setBrandId(entity.getBrand().getId());
            dto.setBrandName(entity.getBrand().getName());
        }

        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierTradeName(entity.getSupplier().getTradeName());
        }

        return dto;
    }
}