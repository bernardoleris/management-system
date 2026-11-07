package br.com.system.data.dto.response;

import br.com.system.enums.CompanyRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyMemberResponseDTO {
    private Long membershipId;
    private Long userId;
    private Long administratorId;
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private CompanyRole role;
    private Boolean active;
}
