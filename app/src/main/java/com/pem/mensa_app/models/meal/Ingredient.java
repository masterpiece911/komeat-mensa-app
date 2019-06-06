package com.pem.mensa_app.models.meal;

public class Ingredient {

    private final String id;

    private final String description;

    public Ingredient(String id, String description) {
        this.id = id;
        this.description = description;

    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
