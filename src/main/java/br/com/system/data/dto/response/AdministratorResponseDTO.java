package br.com.system.data.dto.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class AdministratorResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private UserEntityResponseDTO user;
    private String login;
    private LocalDateTime lastLogin;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean active;
    private List<String> permissions;

    public AdministratorResponseDTO() {}
}
