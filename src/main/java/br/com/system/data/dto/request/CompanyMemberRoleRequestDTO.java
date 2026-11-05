package br.com.system.data.dto.request;

import br.com.system.enums.CompanyRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyMemberRoleRequestDTO {
    @NotNull
    private CompanyRole role;
}
