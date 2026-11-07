package br.com.system.data.dto.request;

import br.com.system.enums.ExitReason;
import br.com.system.enums.MovementType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    private Long supplierId;

    @NotNull(message = "Movement type is required")
    private MovementType type;

    private ExitReason exitReason;

    @Size(max = 255, message = "Reason must have at most 255 characters")
    private String reason;

    @Size(max = 255, message = "Observation must have at most 255 characters")
    private String observation;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<StockMovementItemRequestDTO> items;

    public StockMovementRequestDTO() {}
}