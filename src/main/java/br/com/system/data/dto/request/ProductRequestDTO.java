package br.com.system.data.dto.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
public class ProductRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private String barcode;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Integer quantity;
    private Boolean active;
    private Long categoryId;
    private Long brandId;
    private Long supplierId;

    public ProductRequestDTO() {}
}
