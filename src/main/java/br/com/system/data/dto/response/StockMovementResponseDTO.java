package br.com.system.data.dto.response;

import br.com.system.enums.ExitReason;
import br.com.system.enums.MovementType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class StockMovementResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private MovementType type;
    private String typeLabel;
    private ExitReason exitReason;
    private String exitReasonLabel;
    private String reason;
    private String observation;
    private LocalDateTime date;

    private Long adminId;
    private String adminLogin;

    private Long supplierId;
    private String supplierTradeName;

    private Long saleId;

    private List<StockMovementItemResponseDTO> items;

    public StockMovementResponseDTO() {}
}