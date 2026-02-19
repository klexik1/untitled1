package com.karandashov.labs.model;

/**
 * Перечисление полов персонажей из Rick and Morty.
 * Значения соответствуют полю "gender" в CSV-файле.
 */
public enum Gender {
    MALE,
    FEMALE,
    UNKNOWN,
    GENDERLESS;

    /**
     * Преобразует строку из CSV в значение перечисления.
     * Если строка не совпадает ни с одним значением —
     * возвращается UNKNOWN.
     *
     * @param raw строка из поля gender в CSV
     * @return соответствующее значение Gender
     */
    public static Gender fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return UNKNOWN;
        }
        return switch (raw.trim().toLowerCase()) {
            case "male"       -> MALE;
            case "female"     -> FEMALE;
            case "genderless" -> GENDERLESS;
            default           -> UNKNOWN;
        };
    }
}
