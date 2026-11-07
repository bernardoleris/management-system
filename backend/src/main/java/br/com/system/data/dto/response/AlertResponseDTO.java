package br.com.system.data.dto.response;

import br.com.system.enums.AlertType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
public class AlertResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private String productName;
    private Long adminId;
    private String adminLogin;
    private AlertType type;
    private Integer minimumQuantity;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Boolean read;
    private Boolean active;

    public AlertResponseDTO() {}
}
