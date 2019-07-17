package com.pem.mensa_app.models.imageUpoald;

import com.google.firebase.firestore.DocumentSnapshot;
import com.pem.mensa_app.models.meal.Meal;

/** This class represents the list of all dishes of the day.
 * This is needed for the image upload, in which the user selects the dishes,
 * which are represented on the image. */

public class MealSelected {

    private String uid;
    private String mDescription;
    private boolean mIsSelected;

    public MealSelected(String uid, String description, boolean selected) {
        this.uid = uid;
        this.mDescription = description;
        this.mIsSelected = selected;
    }

    public MealSelected(Meal meal) {
        this.uid = meal.getUid();
        this.mDescription = meal.getName();
        this.mIsSelected = false;
    }

    public MealSelected(DocumentSnapshot documentSnapshot, boolean isSelected) {
        this.uid = documentSnapshot.getId();
        this.mDescription = documentSnapshot.getString("name");
        this.mIsSelected = isSelected;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public boolean ismIsSelected() {
        return mIsSelected;
    }

    public void setmIsSelected(boolean mIsSelected) {
        this.mIsSelected = mIsSelected;
    }

}
