package com.khalchukov.labs.model;

/**
 * Модель персонажа из Rick and Morty.
 * Поля соответствуют столбцам CSV-файла.
 */
public class Character {

    private int    id;
    private String name;
    private String status;
    private String species;
    private String type;
    private Gender gender;
    private String originName;
    private String locationName;
    private String created;

    // ──────────────────── Конструкторы ────────────────────

    public Character() {}

    public Character(int id, String name, String status, String species,
                     String type, Gender gender, String originName,
                     String locationName, String created) {
        this.id           = id;
        this.name         = name;
        this.status       = status;
        this.species      = species;
        this.type         = type;
        this.gender       = gender;
        this.originName   = originName;
        this.locationName = locationName;
        this.created      = created;
    }

    // ──────────────────── CSV-хелперы ────────────────────

    /** Заголовок CSV. */
    public static String csvHeader() {
        return "id,name,status,species,type,gender,origin/name,location/name,created";
    }


    // ──────────────────── toString ────────────────────

    @Override
    public String toString() {
        return String.format("Character{id=%d, name='%s', gender=%s, status='%s'}",
                id, name, gender, status);
    }

    // ──────────────────── Getters / Setters ────────────────────

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }

    public String getStatus()                { return status; }
    public void setStatus(String status)     { this.status = status; }

    public String getSpecies()               { return species; }
    public void setSpecies(String species)   { this.species = species; }

    public String getType()                  { return type; }
    public void setType(String type)         { this.type = type; }

    public Gender getGender()                { return gender; }
    public void setGender(Gender gender)     { this.gender = gender; }

    public String getOriginName()            { return originName; }
    public void setOriginName(String o)      { this.originName = o; }

    public String getLocationName()          { return locationName; }
    public void setLocationName(String l)    { this.locationName = l; }

    public String getCreated()               { return created; }
    public void setCreated(String created)   { this.created = created; }
}
