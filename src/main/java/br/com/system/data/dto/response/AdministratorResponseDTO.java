package br.com.system.data.dto.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import br.com.system.enums.CompanyRole;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
public class AdministratorResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String login;
    private LocalDateTime lastLogin;
    private CompanyRole role;
    private UserEntityResponseDTO user;

    public AdministratorResponseDTO() {}
}
