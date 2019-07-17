package com.pem.mensa_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.pem.mensa_app.meal_detail_activity.MealDetailActivity;
import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.ui.home_activity.HomePagerAdapter;
import com.pem.mensa_app.ui.home_activity.HomeFeedItemsListener;

public class HomeActivity extends AppCompatActivity implements HomeFeedItemsListener {

    static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        HomePagerAdapter homePagerAdapter = new HomePagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.home_viewpager);
        viewPager.setAdapter(homePagerAdapter);
        TabLayout tabs = findViewById(R.id.home_tabs);
        tabs.setupWithViewPager(viewPager);

        Toolbar toolbar = findViewById(R.id.home_toolbar);
        toolbar.setTitle("  kom.eat");
//        toolbar.setTitleTextAppearance(this, R.style.Komeat_Text_Header);
        toolbar.setLogo(R.drawable.ic_glyph);

    }

    @Override
    public void onCustomizeClicked() {
        Intent intent = new Intent(HomeActivity.this, FavoritePickerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMensaSelected(Mensa mensa) {
        Log.d(TAG, "onMensaSelected: selected " + mensa.getName());
        Intent intent = new Intent(HomeActivity.this, MensaMealListActivity.class);
        intent.putExtra(getString(R.string.intent_mensa_uid), mensa.getuID());
        intent.putExtra(getString(R.string.intent_mensa_name), mensa.getName());
        if(mensa.getMealPlanReference() != null) {
            intent.putExtra(getString(R.string.intent_mensa_meal_plan_reference_path), mensa.getMealPlanReference());
        }
        if(mensa.getUrl() != null) {
            intent.putExtra(getString(R.string.intent_mensa_eatapi_url), mensa.getUrl());
        }

        startActivity(intent);

    }

    @Override
    public void onImageClicked(Mensa mensa, Meal meal) {
        Intent intent = new Intent(HomeActivity.this, MealDetailActivity.class);
        intent.putExtra("INTENT_MEAL_UID", meal.getUid());
        intent.putExtra("meal_path", mensa.getMealPlanReference());
        intent.putExtra("day", meal.getWeekday());
        startActivity(intent);
    }
}
