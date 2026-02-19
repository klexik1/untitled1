package com.karandashov.labs.util;

import com.karandashov.labs.model.Character;
import com.karandashov.labs.model.Gender;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Упрощенный парсер CSV без поддержки кавычек и экранирования.
 * Предполагает, что поля НЕ содержат запятых внутри.
 */
public class CsvParser {

    private CsvParser() {}

    /**
     * Читает персонажей из файла, пропуская заголовок.
     */
    public static List<Character> readAll(Path path) throws IOException {
        List<Character> characters = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // пропускаем заголовок
            if (line == null) return characters;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                try {
                    characters.add(parseLine(line));
                } catch (Exception e) {
                    System.err.println("Пропускаем некорректную строку: " + line);
                    System.err.println("Причина: " + e.getMessage());
                }
            }
        }
        return characters;
    }

    /**
     * Разбирает одну строку CSV в объект Character.
     * Упрощенный парсинг - просто split по запятой.
     */
    private static Character parseLine(String line) {
        // ПРОСТОЙ split без учета кавычек
        String[] parts = line.split(",", -1); // -1 чтобы сохранить пустые поля в конце

        // Очищаем каждое поле от лишних пробелов
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        int    id           = Integer.parseInt(parts[0]);
        String name         = parts[1];
        String status       = parts[2];
        String species      = parts[3];
        String type         = parts[4];
        Gender gender       = Gender.fromString(parts[5]);
        String originName   = parts[6];
        String locationName = parts[7];
        String created      = parts.length > 8 ? parts[8] : "";

        return new Character(id, name, status, species, type,
                gender, originName, locationName, created);
    }

    /**
     * Записывает список персонажей в CSV-файл (с заголовком).
     */
    public static void writeAll(Path path, List<Character> characters) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(Character.csvHeader());
            writer.newLine();

            for (Character c : characters) {
                // Упрощенная запись - просто соединяем запятыми
                writer.write(String.join(",",
                        String.valueOf(c.getId()),
                        c.getName(),
                        c.getStatus(),
                        c.getSpecies(),
                        c.getType(),
                        c.getGender().name().charAt(0) + c.getGender().name().substring(1).toLowerCase(),
                        c.getOriginName(),
                        c.getLocationName(),
                        c.getCreated()
                ));
                writer.newLine();
            }
        }
    }
}