package br.com.system.data.dto.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
public class ProductResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private String barcode;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Integer quantity;
    private LocalDateTime createdAt;
    private Boolean active;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long supplierId;
    private String supplierTradeName;

    public ProductResponseDTO() {}
}
