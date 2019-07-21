package com.pem.mensa_app.utilities.eatapi;

import android.graphics.Color;
import android.util.Pair;

import java.util.List;

public class MealIngredientParser {

    public static Pair<String, Integer> getPillFromIngredients(List<String> ingredients) {

        String name; int color;

        if (ingredients.contains("S")) {
            name = "pork";
            color = Color.parseColor("#FF488A");
        }else if (ingredients.contains("R")) {
            color = Color.parseColor("#5D2F00");
            name = "beef";
        } else if (ingredients.contains("K")) {
            color = Color.parseColor("#5D2F00");
            name = "veal";
        } else if (ingredients.contains("W")) {
            color = Color.parseColor("#5D2F00");
            name = "wild meat";
        } else if (ingredients.contains("L")) {
            color = Color.parseColor("#5D2F00");
            name = "lamb";
        } else if (ingredients.contains("G")) {
            color = Color.parseColor("#FF9E21");
            name = "poultry";
        } else if (ingredients.contains("Fi")){
            color = Color.parseColor("#32A1EF");
            name = "fish";
        } else if (ingredients.contains("Kr") || ingredients.contains("Wt")){
            color = Color.parseColor("#32A1EF");
            name = "seafood";
        } else if (ingredients.contains("Ei") || ingredients.contains("Mi")) {
            color = Color.parseColor("#FF9E21");
            name = "meat-free";
        } else {
            name = "vegan";
            color = Color.parseColor("#39660D");
        }

        return new Pair<>(name, color);

    }

}
