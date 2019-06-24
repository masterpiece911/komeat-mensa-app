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

    private String uid;

    private String name;

    private Double price;

    private List<Ingredient> ingredients;

    private List<String> comments;

    private List<String> images;

    public Meal(String name, Double price, List<Ingredient> ingredients, List<String> comments, List<String> images) {
        this.name = name;
        this.price = price;
        this.ingredients = ingredients;
        this.comments = comments;
        this.images = images;
    }

    public Meal() {

    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getUid() {
        return uid;
    }

    public List<String> getComments() {
        return comments;
    }

    public List<String> getImages() {
        return images;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
