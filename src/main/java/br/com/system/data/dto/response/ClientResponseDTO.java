package br.com.system.data.dto.response;

import br.com.system.enums.DocumentType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode
public class ClientResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private UserEntityResponseDTO user;
    private DocumentType documentType;
    private String documentNumber;
    private LocalDate birthDate;
    private AddressResponseDTO address;

    public ClientResponseDTO() {}
}
