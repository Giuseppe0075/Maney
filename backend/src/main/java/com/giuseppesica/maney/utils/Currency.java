package com.giuseppesica.maney.utils;

import lombok.Getter;

@Getter
public enum Currency {
    EUR("EUR", "€", "Euro"),
    USD("USD", "$", "US Dollar"),
    GBP("GBP", "£", "British Pound"),
    JPY("JPY", "¥", "Japanese Yen"),
    CHF("CHF", "CHF", "Swiss Franc"),
    AUD("AUD", "A$", "Australian Dollar"),
    CAD("CAD", "C$", "Canadian Dollar");

    private final String code;
    private final String symbol;
    private final String name;

    Currency(String code, String symbol, String name) {
        this.code = code;
        this.symbol = symbol;
        this.name = name;
    }
}
