package br.com.system.data.dto.response;

import br.com.system.enums.CompanyRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailableCompanyResponseDTO {
    private Long companyId;
    private String companyName;
    private CompanyRole role;
}
