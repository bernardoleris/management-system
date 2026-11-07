package br.com.system.data.dto.response;

import br.com.system.enums.DocumentType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode
public class ClientResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private DocumentType documentType;
    private String documentNumber;
    private LocalDate birthDate;
    private UserEntityResponseDTO user;
    private AddressResponseDTO address;

    public ClientResponseDTO() {}
}