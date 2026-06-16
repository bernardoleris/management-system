package br.com.system.data.dto.request;

import br.com.system.enums.ExitReason;
import br.com.system.enums.MovementType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class StockMovementRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long adminId;
    private Long supplierId;
    private MovementType type;
    private ExitReason exitReason;
    private String reason;
    private String observation;
    private List<StockMovementItemRequestDTO> items;

    public StockMovementRequestDTO() {}
}