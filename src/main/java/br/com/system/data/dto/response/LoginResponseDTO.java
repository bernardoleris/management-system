package br.com.system.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String login;
    private Boolean authenticated;
    private Date issuedAt;
    private Date expiration;
    private String token;
}