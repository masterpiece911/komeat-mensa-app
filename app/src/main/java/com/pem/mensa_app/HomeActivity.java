package com.pem.mensa_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.ui.home_activity.HomePagerAdapter;
import com.pem.mensa_app.ui.home_activity.OnMensaItemAndCustomizeSelectedListener;

public class HomeActivity extends AppCompatActivity implements OnMensaItemAndCustomizeSelectedListener {

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

}
