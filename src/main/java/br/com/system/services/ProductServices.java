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
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<ProductResponseDTO> findAll() {
        logger.info("Finding products!");

        return productRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<ProductResponseDTO> findActive() {
        logger.info("Finding active products!");

        return productRepository.findByActiveTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<ProductResponseDTO> findByCategory(Long categoryId) {
        logger.info("Finding products by category!");

        findCategory(categoryId);

        return productRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<ProductResponseDTO> findByBrand(Long brandId) {
        logger.info("Finding products by brand!");

        findBrand(brandId);

        return productRepository.findByBrandIdAndActiveTrue(brandId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<ProductResponseDTO> findBySupplier(Long supplierId) {
        logger.info("Finding products by supplier!");

        findSupplier(supplierId);

        return productRepository.findBySupplierIdAndActiveTrue(supplierId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ProductResponseDTO findById(Long id) {
        logger.info("Finding one product!");

        return toResponseDTO(findProduct(id));
    }

    public ProductResponseDTO findByBarcode(String barcode) {
        logger.info("Finding one product by barcode!");

        Product entity = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("No product found for this barcode!"));

        return toResponseDTO(entity);
    }

    public ProductResponseDTO create(ProductRequestDTO product) {
        logger.info("Creating one product!");

        Product entity = new Product();

        if (productRepository.existsByBarcode(product.getBarcode())) {
            throw new DuplicateResourceException("Barcode already registered!");
        }

        setProductFields(entity, product);

        return toResponseDTO(productRepository.save(entity));
    }

    public ProductResponseDTO update(Long id, ProductRequestDTO product) {
        logger.info("Updating one product!");

        Product entity = findProduct(id);

        if (productRepository.existsByBarcodeAndIdNot(product.getBarcode(), id)) {
            throw new DuplicateResourceException("Barcode already registered!");
        }

        setProductFields(entity, product);

        return toResponseDTO(productRepository.save(entity));
    }

    public void toggleActive(Long id) {
        logger.info("Toggling product active status!");

        Product entity = findProduct(id);

        entity.setActive(!entity.getActive());
        productRepository.save(entity);
    }

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

    private void setProductFields(Product entity, ProductRequestDTO product) {
        Category category = findCategory(product.getCategoryId());
        Brand brand = findBrand(product.getBrandId());
        Supplier supplier = findSupplier(product.getSupplierId());

        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setBarcode(product.getBarcode());
        entity.setPurchasePrice(product.getPurchasePrice());
        entity.setSalePrice(product.getSalePrice());
        entity.setQuantity(product.getQuantity());
        entity.setCategory(category);
        entity.setBrand(brand);
        entity.setSupplier(supplier);

        if (product.getActive() != null) {
            entity.setActive(product.getActive());
        }
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
