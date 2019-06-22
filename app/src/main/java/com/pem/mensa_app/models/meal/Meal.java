package com.pem.mensa_app.models.meal;

import java.util.List;

/**
 * This class represents the dishes.
 * A dish has a name, a price and a number of ingredients.
 * Prise is in String format, because it isnt clear, what we will get here; could be a sting, like:
 * <li>individual</li>
 * <li>N/A</li>
 * <li>0.75€ / 100g</li>
 */

public class Meal {

    private final String name;

    private final String price;

    private final List<Ingredient> ingredients;

    private final List<String> comments;

    private final List<String> images;

    public Meal(String name, String price, List<Ingredient> ingredients, List<String> comments, List<String> images) {
        this.name = name;
        this.price = price;
        this.ingredients = ingredients;
        this.comments = comments;
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
