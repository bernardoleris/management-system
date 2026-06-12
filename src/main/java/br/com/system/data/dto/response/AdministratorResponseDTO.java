package br.com.system.data.dto.response;

import br.com.system.enums.AdministratorRole;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
public class AdministratorResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private UserEntityResponseDTO user;
    private String login;
    private AdministratorRole role;
    private LocalDateTime lastLogin;

    public AdministratorResponseDTO() {}
}
