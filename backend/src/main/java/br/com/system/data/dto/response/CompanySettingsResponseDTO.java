package br.com.system.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CompanySettingsResponseDTO {
    private Long companyId;
    private String legalName;
    private String tradeName;
    private String documentNumber;
    private String slug;
    private String email;
    private String phone;
    private Boolean active;
    private String timezone;
    private String currency;
    private LocalDateTime createdAt;
}
