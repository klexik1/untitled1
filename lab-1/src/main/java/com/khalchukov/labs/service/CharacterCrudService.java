package com.khalchukov.labs.service;

import com.khalchukov.labs.model.Character;
import com.khalchukov.labs.model.Gender;
import com.khalchukov.labs.util.CsvParser;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * CRUD-сервис для управления персонажами в CSV-файле.
 *
 * <p>Все операции работают по схеме «загрузить → изменить → сохранить»,
 * что соответствует файловому хранилищу без БД.
 *
 * <h2>Операции:</h2>
 * <ul>
 *   <li>{@link #findAll}   — получить всех персонажей</li>
 *   <li>{@link #findById}  — найти по ID</li>
 *   <li>{@link #create}    — добавить нового</li>
 *   <li>{@link #update}    — обновить существующего</li>
 *   <li>{@link #delete}    — удалить по ID</li>
 * </ul>
 */
public class CharacterCrudService {

    private final Path csvPath;

    public CharacterCrudService(Path csvPath) {
        this.csvPath = csvPath;
    }

    // ══════════════════════════════════════════════
    //  READ
    // ══════════════════════════════════════════════

    /**
     * Возвращает всех персонажей из файла.
     */
    public List<Character> findAll() throws IOException {
        return CsvParser.readAll(csvPath);
    }

    /**
     * Ищет персонажа по идентификатору.
     *
     * @param id идентификатор
     * @return Optional с персонажем или Optional.empty()
     */
    public Optional<Character> findById(int id) throws IOException {
        return findAll().stream()
                .filter(c -> c.getId() == id)
                .findFirst();
    }

    // ══════════════════════════════════════════════
    //  CREATE
    // ══════════════════════════════════════════════

    /**
     * Добавляет нового персонажа в CSV-файл.
     * ID генерируется автоматически: max(существующих) + 1.
     *
     * @param name         имя персонажа
     * @param status       статус (Alive / Dead / unknown)
     * @param species      вид (Human, Alien, …)
     * @param type         тип (может быть пустым)
     * @param gender       пол
     * @param originName   место происхождения
     * @param locationName текущая локация
     * @return созданный объект Character
     */
    public Character create(String name, String status, String species,
                            String type, Gender gender,
                            String originName, String locationName) throws IOException {

        List<Character> all = findAll();

        int newId = all.stream()
                .mapToInt(Character::getId)
                .max()
                .orElse(0) + 1;

        Character newChar = new Character(
                newId, name, status, species, type, gender,
                originName, locationName, Instant.now().toString()
        );

        all.add(newChar);
        CsvParser.writeAll(csvPath, all);

        System.out.printf("[CREATE] Создан персонаж: id=%d, name='%s', gender=%s%n",
                newChar.getId(), newChar.getName(), newChar.getGender());
        return newChar;
    }

    // ══════════════════════════════════════════════
    //  UPDATE
    // ══════════════════════════════════════════════

    /**
     * Обновляет поля существующего персонажа.
     * Null-значение в параметре означает «не изменять поле».
     *
     * @param id           ID персонажа для обновления
     * @param name         новое имя (или null)
     * @param status       новый статус (или null)
     * @param species      новый вид (или null)
     * @param type         новый тип (или null)
     * @param gender       новый пол (или null)
     * @param originName   новое место происхождения (или null)
     * @param locationName новая локация (или null)
     * @return Optional с обновлённым персонажем или empty если не найден
     */
    public Optional<Character> update(int id,
                                      String name, String status, String species,
                                      String type, Gender gender,
                                      String originName, String locationName)
            throws IOException {

        List<Character> all = findAll();
        Optional<Character> found = all.stream()
                .filter(c -> c.getId() == id)
                .findFirst();

        if (found.isEmpty()) {
            System.out.printf("[UPDATE] Персонаж с id=%d не найден.%n", id);
            return Optional.empty();
        }

        Character c = found.get();
        if (name         != null) c.setName(name);
        if (status       != null) c.setStatus(status);
        if (species      != null) c.setSpecies(species);
        if (type         != null) c.setType(type);
        if (gender       != null) c.setGender(gender);
        if (originName   != null) c.setOriginName(originName);
        if (locationName != null) c.setLocationName(locationName);

        CsvParser.writeAll(csvPath, all);

        System.out.printf("[UPDATE] Обновлён персонаж: id=%d, name='%s', gender=%s%n",
                c.getId(), c.getName(), c.getGender());
        return Optional.of(c);
    }

    // ══════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════

    /**
     * Удаляет персонажа по ID.
     *
     * @param id идентификатор
     * @return true если удалён, false если не найден
     */
    public boolean delete(int id) throws IOException {
        List<Character> all = findAll();
        boolean removed = all.removeIf(c -> c.getId() == id);

        if (removed) {
            CsvParser.writeAll(csvPath, all);
            System.out.printf("[DELETE] Удалён персонаж с id=%d%n", id);
        } else {
            System.out.printf("[DELETE] Персонаж с id=%d не найден.%n", id);
        }
        return removed;
    }

}
