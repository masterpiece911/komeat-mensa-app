package com.pem.mensa_app.models.meal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private List<String> ingredients;

    private ArrayList<String> comments;

    private ArrayList<String> images;

    public Meal(String uid, String name, Double price, List<String> ingredients, ArrayList<String> comments, ArrayList<String> images) {
        this.uid = uid;
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

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getUid() {
        return uid;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> mealMetadata = new HashMap<>();
        mealMetadata.put("name", getName());
        mealMetadata.put("price", getPrice());
        mealMetadata.put("ingredients", getIngredients());
        mealMetadata.put("comments", getComments());
        mealMetadata.put("imagePaths", getImages());

        return mealMetadata;
    }
}
