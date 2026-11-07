package br.com.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MovementType {

    ENTRY("Entrada"),
    EXIT("Saída"),
    ADJUSTMENT("Ajuste"),
    SALE("Venda");

    private final String label;
}