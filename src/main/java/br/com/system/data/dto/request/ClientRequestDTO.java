package br.com.system.data.dto.request;

import br.com.system.enums.DocumentType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode
public class ClientRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private DocumentType documentType;
    private String documentNumber;
    private LocalDate birthDate;
    private Long addressId;

    public ClientRequestDTO() {}
}
