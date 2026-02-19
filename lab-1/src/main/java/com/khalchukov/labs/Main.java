package com.khalchukov.labs;

import com.khalchukov.labs.model.Character;
import com.khalchukov.labs.model.Gender;
import com.khalchukov.labs.service.CharacterCrudService;
import com.khalchukov.labs.service.GenderCountService;


import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static CharacterCrudService crud;
    private static GenderCountService genderService;
    private static Path csvPath;
    private static Path workDir;

    public static void main(String[] args) throws IOException, URISyntaxException {

        URL resource = Main.class.getClassLoader().getResource("characters.csv");
        if (resource == null) {
            System.err.println("Файл characters.csv не найден в ресурсах!");
            return;
        }

        workDir = Paths.get(System.getProperty("user.dir"), "output");
        Files.createDirectories(workDir);

        csvPath = workDir.resolve("characters.csv");
        Files.copy(Paths.get(resource.toURI()), csvPath, StandardCopyOption.REPLACE_EXISTING);

        crud = new CharacterCrudService(csvPath);
        genderService = new GenderCountService();


        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("Выберите пункт").trim();
            System.out.println();

            switch (choice) {
                case "1" -> showAll();
                case "2" -> findById();
                case "3" -> showAndSaveGenderStats();
                case "4" -> createCharacter();
                case "5" -> updateCharacter();
                case "6" -> deleteCharacter();
                case "0" -> {
                    running = false;
                    System.out.println("До свидания!");
                    try {
                        saveStatsOnExit();
                    } catch (IOException e) {
                        System.err.println("Не удалось сохранить статистику: " + e.getMessage());
                    }
                }
                default -> System.out.println("Неизвестный пункт. Попробуйте снова.");
            }
        }

        SCANNER.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│               МЕНЮ                  │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  1. Показать всех персонажей        │");
        System.out.println("│  2. Найти по ID                     │");
        System.out.println("│  3. Статистика по полу (EnumMap)    │");
        System.out.println("│     (показывает и сохраняет в файл) │");
        System.out.println("│  4. Добавить персонажа   [CREATE]   │");
        System.out.println("│  5. Обновить персонажа   [UPDATE]   │");
        System.out.println("│  6. Удалить персонажа    [DELETE]   │");
        System.out.println("│  0. Выход                           │");
        System.out.println("└─────────────────────────────────────┘");
    }

    private static void showAll() throws IOException {
        List<Character> all = crud.findAll();
        if (all.isEmpty()) {
            System.out.println("Список персонажей пуст.");
            return;
        }
        System.out.printf("%-5s %-30s %-12s %-12s %-10s%n", "ID", "Имя", "Пол", "Статус", "Вид");
        System.out.println("─".repeat(72));
        for (Character c : all) {
            System.out.printf("%-5d %-30s %-12s %-12s %-10s%n",
                    c.getId(), truncate(c.getName(), 29),
                    c.getGender().name(), c.getStatus(), c.getSpecies());
        }
        System.out.println("─".repeat(72));
        System.out.printf("Всего: %d персонажей%n", all.size());
    }

    private static void findById() throws IOException {
        int id = readInt("Введите ID персонажа");
        Optional<Character> found = crud.findById(id);
        if (found.isPresent()) {
            printCharacterDetails(found.get());
        } else {
            System.out.println("Персонаж с ID=" + id + " не найден.");
        }
    }

    private static void showAndSaveGenderStats() throws IOException {
        List<Character> all = crud.findAll();
        EnumMap<Gender, Integer> counts = genderService.countByGender(all);

        genderService.printResult(counts);
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path statsPath = workDir.resolve("gender_stats_" + timestamp + ".txt");
        genderService.saveStatsToFile(statsPath, counts);
    }

    private static void saveStatsOnExit() throws IOException {
        EnumMap<Gender, Integer> counts = genderService.countByGender(crud.findAll());
        Path statsPath = workDir.resolve("gender_stats_final.txt");
        genderService.saveStatsToFile(statsPath, counts);
    }

    private static void createCharacter() throws IOException {
        System.out.println("── Добавление нового персонажа ──");
        String name = promptRequired("Имя");
        String status = promptWithHint("Статус", "Alive / Dead / unknown");
        String species = promptWithHint("Вид", "Human / Alien / Robot …");
        String type = prompt("Тип (Enter — пропустить)").trim();
        Gender gender = readGender();
        String originName = promptWithHint("Место происхождения", "Earth (C-137) / unknown …");
        String locationName = promptWithHint("Текущая локация", "Earth (C-137) / unknown …");

        Character created = crud.create(name, status, species, type,
                gender, originName, locationName);
        System.out.println("\n Персонаж успешно создан:");
        printCharacterDetails(created);
    }

    private static void updateCharacter() throws IOException {
        int id = readInt("Введите ID персонажа для обновления");
        Optional<Character> existing = crud.findById(id);
        if (existing.isEmpty()) {
            System.out.println("Персонаж с ID=" + id + " не найден.");
            return;
        }

        System.out.println("Текущие данные:");
        printCharacterDetails(existing.get());
        System.out.println("\nВведите новые значения (Enter — оставить без изменений):");

        String name = promptOptional("Имя", existing.get().getName());
        String status = promptOptional("Статус", existing.get().getStatus());
        String species = promptOptional("Вид", existing.get().getSpecies());
        String type = promptOptional("Тип", existing.get().getType());
        Gender gender = readGenderOptional(existing.get().getGender());
        String originName = promptOptional("Место происхождения", existing.get().getOriginName());
        String locationName = promptOptional("Текущая локация", existing.get().getLocationName());

        crud.update(id, name, status, species, type, gender, originName, locationName)
                .ifPresent(c -> {
                    System.out.println("\n Обновлено:");
                    printCharacterDetails(c);
                });
    }

    private static void deleteCharacter() throws IOException {
        int id = readInt("Введите ID персонажа для удаления");
        Optional<Character> existing = crud.findById(id);
        if (existing.isEmpty()) {
            System.out.println("Персонаж с ID=" + id + " не найден.");
            return;
        }

        System.out.println("Вы хотите удалить:");
        printCharacterDetails(existing.get());
        String confirm = prompt("Подтвердить удаление? (да/нет)").trim().toLowerCase();

        if (confirm.equals("да") || confirm.equals("д") || confirm.equals("y") || confirm.equals("yes")) {
            crud.delete(id);
            System.out.println(" Персонаж удалён.");
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    private static void printCharacterDetails(Character c) {
        System.out.println("  ID            : " + c.getId());
        System.out.println("  Имя           : " + c.getName());
        System.out.println("  Пол           : " + c.getGender().name());
        System.out.println("  Статус        : " + c.getStatus());
        System.out.println("  Вид           : " + c.getSpecies());
        System.out.println("  Тип           : " + (c.getType().isBlank() ? "—" : c.getType()));
        System.out.println("  Происхождение : " + c.getOriginName());
        System.out.println("  Локация       : " + c.getLocationName());
        System.out.println("  Создан        : " + c.getCreated());
    }

    private static Gender readGender() {
        while (true) {
            System.out.print("  Пол (MALE / FEMALE / UNKNOWN / GENDERLESS): ");
            String raw = SCANNER.nextLine().trim().toLowerCase();
            switch (raw) {
                case "male": return Gender.MALE;
                case "female": return Gender.FEMALE;
                case "unknown": return Gender.UNKNOWN;
                case "genderless": return Gender.GENDERLESS;
                default: System.out.println(" Допустимо: MALE, FEMALE, UNKNOWN, GENDERLESS");
            }
        }
    }

    private static Gender readGenderOptional(Gender current) {
        System.out.printf("  Пол (MALE / FEMALE / UNKNOWN / GENDERLESS) [%s]: ", current.name());
        String raw = SCANNER.nextLine().trim().toLowerCase();
        if (raw.isEmpty()) return null;
        return switch (raw) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            case "unknown" -> Gender.UNKNOWN;
            case "genderless" -> Gender.GENDERLESS;
            default -> {
                System.out.println("Некорректный пол, оставлен без изменений.");
                yield null;
            }
        };
    }

    private static String prompt(String label) {
        System.out.print("  " + label + ": ");
        return SCANNER.nextLine();
    }

    private static String promptWithHint(String label, String hint) {
        System.out.print("  " + label + " (" + hint + "): ");
        return SCANNER.nextLine().trim();
    }

    private static String promptRequired(String label) {
        while (true) {
            System.out.print("  " + label + ": ");
            String value = SCANNER.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("  Поле обязательно для заполнения.");
        }
    }

    private static String promptOptional(String label, String current) {
        System.out.printf("  %s [%s]: ", label, current.isBlank() ? "—" : current);
        String value = SCANNER.nextLine().trim();
        return value.isEmpty() ? null : value;
    }

    private static int readInt(String label) {
        while (true) {
            System.out.print("  " + label + ": ");
            try {
                return Integer.parseInt(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  Введите целое число.");
            }
        }
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }
}