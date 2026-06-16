package br.com.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExitReason {

    DAMAGED("Danificado"),
    EXPIRED("Vencido"),
    DONATED("Brinde/Doação"),
    LOST("Perda/Furto"),
    OTHER("Outro");

    private final String label;
}