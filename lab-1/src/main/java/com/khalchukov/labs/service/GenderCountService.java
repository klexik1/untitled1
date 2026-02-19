package com.khalchukov.labs.service;

import com.khalchukov.labs.model.Character;
import com.khalchukov.labs.model.Gender;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GenderCountService {

    /**
     * Подсчитывает количество персонажей каждого пола.
     */
    public EnumMap<Gender, Integer> countByGender(List<Character> characters) {
        EnumMap<Gender, Integer> counts = new EnumMap<>(Gender.class);

        // Инициализируем все ключи нулями
        for (Gender g : Gender.values()) {
            counts.put(g, 0);
        }

        // Подсчитываем
        for (Character character : characters) {
            Gender gender = character.getGender();
            counts.merge(gender, 1, Integer::sum);
        }

        return counts;
    }

    /**
     * Сохраняет статистику в файл с красивым форматированием.
     * Теперь это один метод, который и показывает, и сохраняет.
     */
    public void saveStatsToFile(Path path, EnumMap<Gender, Integer> counts) throws IOException {
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();

        // Добавляем временную метку
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("╔════════════════════════════════════════════════╗");
            writer.newLine();
            writer.write("║     СТАТИСТИКА ПЕРСОНАЖЕЙ RICK AND MORTY      ║");
            writer.newLine();
            writer.write("╚════════════════════════════════════════════════╝");
            writer.newLine();
            writer.write("Дата и время: " + timestamp);
            writer.newLine();
            writer.newLine();

            writer.write("┌────────────┬───────┐");
            writer.newLine();
            writer.write("│    ПОЛ     │ КОЛ-ВО │");
            writer.newLine();
            writer.write("├────────────┼───────┤");
            writer.newLine();

            for (Map.Entry<Gender, Integer> entry : counts.entrySet()) {
                writer.write(String.format("│ %-10s │ %5d │",
                        entry.getKey().name(), entry.getValue()));
                writer.newLine();
            }

            writer.write("├────────────┼───────┤");
            writer.newLine();
            writer.write(String.format("│ %-10s │ %5d │", "ИТОГО", total));
            writer.newLine();
            writer.write("└────────────┴───────┘");
            writer.newLine();

            // Добавляем дополнительную информацию
            writer.newLine();
            writer.write("Всего персонажей в базе: " + total);
        }

    }

    /**
     * Печатает статистику в консоль.
     */
    public void printResult(EnumMap<Gender, Integer> counts) {
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();

        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║     СТАТИСТИКА ПО ПОЛУ (EnumMap)   ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println();

        System.out.println("┌────────────┬───────┐");
        System.out.println("│    ПОЛ     │ КОЛ-ВО │");
        System.out.println("├────────────┼───────┤");

        for (Map.Entry<Gender, Integer> entry : counts.entrySet()) {
            System.out.printf("│ %-10s │ %5d │%n",
                    entry.getKey().name(), entry.getValue());
        }

        System.out.println("├────────────┼───────┤");
        System.out.printf("│ %-10s │ %5d │%n", "ИТОГО", total);
        System.out.println("└────────────┴───────┘");
    }
}