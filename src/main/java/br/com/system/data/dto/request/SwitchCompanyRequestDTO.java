package br.com.system.data.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwitchCompanyRequestDTO {
    @NotNull
    private Long companyId;
}
