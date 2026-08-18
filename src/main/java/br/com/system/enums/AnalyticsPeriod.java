package br.com.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnalyticsPeriod {

    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    LAST_6_MONTHS("Last 6 months"),
    LAST_12_MONTHS("Last 12 months"),
    CURRENT_MONTH("Current month"),
    CURRENT_YEAR("Current year");

    private final String label;
}