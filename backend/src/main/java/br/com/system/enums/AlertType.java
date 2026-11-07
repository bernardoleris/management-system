package br.com.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlertType {
    LOW_STOCK("Estoque baixo"),
    OUT_OF_STOCK("Fora de estoque");

    private final String label;
}