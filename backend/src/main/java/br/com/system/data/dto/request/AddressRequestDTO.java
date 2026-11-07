package br.com.system.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class AddressRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Street is required")
    @Size(max = 100, message = "Street must have at most 100 characters")
    private String street;

    @NotBlank(message = "Number is required")
    @Size(max = 10, message = "Number must have at most 10 characters")
    private String number;

    @Size(max = 50, message = "Complement must have at most 50 characters")
    private String complement;

    @NotBlank(message = "Neighborhood is required")
    @Size(max = 100, message = "Neighborhood must have at most 100 characters")
    private String neighborhood;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must have at most 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 2, message = "State must have exactly 2 characters")
    private String state;

    @NotBlank(message = "Zip code is required")
    @Size(min = 8, max = 8, message = "Zip code must have exactly 8 characters")
    private String zipCode;

    public AddressRequestDTO() {}
}