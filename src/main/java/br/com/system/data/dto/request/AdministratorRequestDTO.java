package br.com.system.data.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class AdministratorRequestDTO implements Serializable {
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

    @NotBlank(message = "Login is required")
    @Size(max = 50, message = "Login must have at most 50 characters")
    private String login;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Permission is required")
    private Long permissionId;

    public AdministratorRequestDTO() {}
}