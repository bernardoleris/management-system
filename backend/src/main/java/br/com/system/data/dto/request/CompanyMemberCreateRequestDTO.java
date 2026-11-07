package br.com.system.data.dto.request;

import br.com.system.enums.CompanyRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyMemberCreateRequestDTO {
    @NotBlank
    @Size(max = 80)
    private String firstName;

    @NotBlank
    @Size(max = 80)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 50)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(max = 50)
    private String login;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotNull
    private CompanyRole role;
}
