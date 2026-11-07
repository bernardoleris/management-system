package br.com.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SaleStatus {
    PENDING("Pendente"),
    COMPLETED("Completo"),
    CANCELED("Cancelado");

    private final String label;
}