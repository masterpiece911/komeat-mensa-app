package com.pem.mensa_app.models.imageUpoald;

import java.util.List;

/** This class represents the list of all dishes of the day.
 * This is needed for the image upload, in which the user selects the dishes,
 * which are represented on the image. */

public class MealSelected {

    private String mDescription;
    private boolean mSelected;

    public MealSelected(String description, boolean selected) {
        this.mDescription = description;
        this.mSelected = selected;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public boolean ismSelected() {
        return mSelected;
    }

    public void setmSelected(boolean mSelected) {
        this.mSelected = mSelected;
    }

}
