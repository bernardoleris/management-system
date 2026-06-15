package br.com.system.data.dto.request;

import br.com.system.enums.MovementType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class StockMovementRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long productId;
    private Long adminId;
    private Long supplierId;
    private MovementType type;
    private Integer quantity;
    private String reason;

    public StockMovementRequestDTO() {}
}
