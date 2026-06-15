package br.com.system.data.dto.request;

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

    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;

    public SaleItemRequestDTO() {}
}
