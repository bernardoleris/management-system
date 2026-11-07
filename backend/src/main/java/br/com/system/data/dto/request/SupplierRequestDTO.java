package br.com.system.data.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierRequestDTO {

    @NotBlank(message = "Trade name is required")
    @Size(max = 100, message = "Trade name must have at most 100 characters")
    private String tradeName;

    @NotBlank(message = "CNPJ is required")
    @Size(min = 14, max = 14, message = "CNPJ must have exactly 14 characters")
    private String cnpj;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone must have at most 20 characters")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must have at most 100 characters")
    private String email;
}