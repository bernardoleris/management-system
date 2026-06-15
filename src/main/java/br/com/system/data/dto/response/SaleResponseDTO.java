package br.com.system.data.dto.response;

import br.com.system.enums.Payment;
import br.com.system.enums.SaleStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class SaleResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private LocalDateTime date;
    private SaleStatus status;
    private Payment paymentMethod;
    private BigDecimal discount;
    private BigDecimal totalValue;
    private String notes;
    private Long adminId;
    private String adminLogin;
    private Long clientId;
    private String clientDocumentNumber;
    private List<SaleItemResponseDTO> items;

    public SaleResponseDTO() {}
}
