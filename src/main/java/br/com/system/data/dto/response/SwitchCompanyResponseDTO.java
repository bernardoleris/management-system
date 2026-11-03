package br.com.system.data.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SwitchCompanyResponseDTO {
    private Long companyId;
    private String companyName;
    private Date issuedAt;
    private Date expiration;
    private String accessToken;
}
