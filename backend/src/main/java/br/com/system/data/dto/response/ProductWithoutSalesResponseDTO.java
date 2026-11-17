package br.com.system.data.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductWithoutSalesResponseDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Long daysSinceLastSale;
}
