package br.com.system.data.dto.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class BrandRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    public BrandRequestDTO() {}
}
