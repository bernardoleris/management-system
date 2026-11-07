package br.com.system.data.dto.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
public class SaleItemResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long saleId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal discount;

    public SaleItemResponseDTO() {}
}
