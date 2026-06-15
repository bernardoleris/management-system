package br.com.system.data.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierResponseDTO {

    private Long id;
    private String tradeName;
    private String cnpj;
    private String phone;
    private String email;
    private Boolean active;
}