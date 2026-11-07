package br.com.system.data.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChartResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> labels;
    private List<Number> data;
}