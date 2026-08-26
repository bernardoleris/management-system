package br.com.system.data.dto.request;

import br.com.system.enums.Payment;
import br.com.system.enums.SaleStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "Payment method is required")
    private Payment paymentMethod;

    @PositiveOrZero(message = "Discount must be greater than or equal to zero")
    private BigDecimal discount;

    @Size(max = 200, message = "Notes must have at most 200 characters")
    private String notes;

    @NotNull(message = "Administrator is required")
    private Long adminId;

    private Long clientId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SaleItemRequestDTO> items;

    public SaleRequestDTO() {}
}
