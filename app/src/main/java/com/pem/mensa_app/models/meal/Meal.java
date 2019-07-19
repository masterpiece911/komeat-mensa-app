package com.pem.mensa_app.models.meal;

import com.google.android.gms.common.wrappers.InstantApps;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.joda.time.Instant;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

    private Date timestamp;

    private int weekday;

    private DocumentReference mealPlanReference;

    private DocumentReference mensaReference;

    private ArrayList<String> ingredients;

    private ArrayList<String> comments;

    private ArrayList<String> images;

    private int likeCounter;

    public Meal(String uid, String name, Date timestamp,  int weekday, DocumentReference mealplanReference, DocumentReference mensaReference, ArrayList<String> ingredients, ArrayList<String> comments, ArrayList<String> images, int likeCounter) {
        this.uid = uid;
        this.name = name;
        this.timestamp = timestamp;
        this.weekday = weekday;
        this.mealPlanReference = mealplanReference;
        this.mensaReference = mensaReference;
        this.ingredients = ingredients;
        this.comments = comments;
        this.images = images;
        this.likeCounter = likeCounter;
    }

    public Meal(DocumentSnapshot documentSnapshot) {
        this.uid = documentSnapshot.getId();
        this.name = documentSnapshot.getString("name");
        this.timestamp = documentSnapshot.getDate("date");
        this.weekday = documentSnapshot.getLong("weekday") != null ? documentSnapshot.getLong("weekday").intValue() : calculateWeekday();
        this.mealPlanReference = documentSnapshot.getDocumentReference("mealplan");
        this.mensaReference = documentSnapshot.getDocumentReference("mensa");
        this.ingredients = (ArrayList<String>) documentSnapshot.get("ingredients");
        this.comments = (ArrayList<String>) documentSnapshot.get("comments");
        this.images = (ArrayList<String>) documentSnapshot.get("imagePaths");
        this.likeCounter = documentSnapshot.getLong("likeCounter") != null ? documentSnapshot.getLong("likeCounter").intValue() : 0;
    }

    public Meal() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public Date getTimestamp() { return this.timestamp; }

    public void setTimestamp(Date timestamp) { this.timestamp =  timestamp; }

    public int getWeekday() { return this.weekday; }

    public void setWeekday (int weekday) { this.weekday = weekday; }

    public DocumentReference getMealPlanReference() { return this.mealPlanReference; }

    public void setMealPlanReference(DocumentReference mealPlanReference) { this.mealPlanReference = mealPlanReference; }

    public DocumentReference getMensaReference() { return mensaReference; }

    public void setMensaReference(DocumentReference mensaReference) { this.mensaReference = mensaReference; }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> mealMetadata = new HashMap<>();
        mealMetadata.put("name", getName());
        mealMetadata.put("price", getPrice());
        mealMetadata.put("date", getTimestamp());
        mealMetadata.put("weekday", getWeekday());
        mealMetadata.put("mealplan", getMealPlanReference());
        mealMetadata.put("mensa", getMensaReference());
        mealMetadata.put("ingredients", getIngredients());
        mealMetadata.put("comments", getComments());
        mealMetadata.put("imagePaths", getImages());
        return mealMetadata;
    }

    public int getLikeCounter() {
        return likeCounter;
    }

    public void setLikeCounter(int likeCounter) {
        this.likeCounter = likeCounter;
    }

    private int calculateWeekday() {
        return Instant.ofEpochMilli(timestamp.getTime()).toDateTime().getDayOfWeek() -1;
    }
}
