package br.com.system.data.dto.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
public class AdministratorRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String login;
    private String password;
    private LocalDateTime lastLogin;

    public AdministratorRequestDTO() {}
}
