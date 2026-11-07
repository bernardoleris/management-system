package br.com.system.data.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
public class SaleItemRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Product is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    @Positive(message = "Unit price must be greater than zero")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", message = "Discount must be greater than or equal to zero")
    private BigDecimal discount;

    public SaleItemRequestDTO() {}
}
