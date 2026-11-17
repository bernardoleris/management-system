package br.com.system.data.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanySettingsRequestDTO {
    @NotBlank
    @Size(max = 150)
    private String legalName;

    @NotBlank
    @Size(max = 150)
    private String tradeName;

    @Size(max = 18)
    private String documentNumber;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(max = 50)
    private String timezone;
}
