package br.com.system.data.dto.response;

import br.com.system.enums.MovementType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
public class StockMovementResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private String productName;
    private Long adminId;
    private String adminLogin;
    private Long supplierId;
    private String supplierTradeName;
    private MovementType type;
    private Integer quantity;
    private String reason;
    private LocalDateTime date;

    public StockMovementResponseDTO() {}
}
