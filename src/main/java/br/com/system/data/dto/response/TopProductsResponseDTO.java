package br.com.system.data.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopProductsResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private Long totalSold;
}