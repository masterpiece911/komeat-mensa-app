package com.pem.mensa_app.ui.home_activity;

import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.models.mensa.Mensa;

public interface HomeFeedItemsListener extends OnMensaItemSelectedListener {

    void onCustomizeClicked();
    void onImageClicked(Mensa mensa, Meal meal);
}

