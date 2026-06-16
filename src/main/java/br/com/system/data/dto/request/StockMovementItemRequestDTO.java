package br.com.system.data.dto.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class StockMovementItemRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long productId;
    private Integer quantity;

    public StockMovementItemRequestDTO() {}
}