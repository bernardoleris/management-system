package br.com.system.data.dto.request;

import br.com.system.enums.DocumentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode
public class ClientRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "First name is required")
    @Size(max = 80, message = "First name must have at most 80 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 80, message = "Last name must have at most 80 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 50, message = "Email must have at most 50 characters")
    private String email;

    @Size(max = 20, message = "Phone must have at most 20 characters")
    private String phone;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document number is required")
    @Size(max = 14, message = "Document number must have at most 14 characters")
    private String documentNumber;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Valid
    private AddressRequestDTO address;

    public ClientRequestDTO() {}
}
