package br.com.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdministratorRole {
    OPERATOR("Operador"),
    ADMIN("Administrador");

    private final String label;
}