package br.com.system.data.dto.request;

import br.com.system.enums.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class AlertRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Product is required")
    private Long productId;

    @NotNull(message = "Alert type is required")
    private AlertType type;

    @NotNull(message = "Minimum quantity is required")
    @Positive(message = "Minimum quantity must be greater than zero")
    private Integer minimumQuantity;

    @NotBlank(message = "Message is required")
    @Size(max = 255, message = "Message must have at most 255 characters")
    private String message;

    public AlertRequestDTO() {}
}
