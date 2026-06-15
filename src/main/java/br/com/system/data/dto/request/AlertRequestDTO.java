package br.com.system.data.dto.request;

import br.com.system.enums.AlertType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class AlertRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private Long adminId;
    private AlertType type;
    private Integer minimumQuantity;
    private String message;
    private Boolean read;
    private Boolean active;

    public AlertRequestDTO() {}
}
