package br.com.system.data.dto.request;

import br.com.system.enums.Payment;
import br.com.system.enums.SaleStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class SaleRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private SaleStatus status;
    private Payment paymentMethod;
    private BigDecimal discount;
    private String notes;
    private Long adminId;
    private Long clientId;
    private List<SaleItemRequestDTO> items;

    public SaleRequestDTO() {}
}
