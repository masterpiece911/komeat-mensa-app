package com.pem.mensa_app.models.mensa;

import com.pem.mensa_app.models.meal.Meal;

import org.joda.time.LocalDate;

import java.util.List;

public class MensaDay {

    private Mensa mensa;
    private LocalDate date;
    private List<Meal> meals;

    public MensaDay() {

    }

    public MensaDay(Mensa mensa, List<Meal> meals) {
        this.mensa = mensa;
        if (meals.isEmpty()) {
            Meal emptyMeal = new Meal();
            emptyMeal.setUid(null);
            emptyMeal.setName("Currently no data available");
            meals.add(emptyMeal);
        }
        this.meals = meals;
    }

    public Mensa getMensa() {
        return mensa;
    }

    public void setMensa(Mensa mensa) {
        this.mensa = mensa;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }
}
