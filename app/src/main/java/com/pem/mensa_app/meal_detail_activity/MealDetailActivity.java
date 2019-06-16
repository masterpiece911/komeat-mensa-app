package com.pem.mensa_app.meal_detail_activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.pem.mensa_app.R;

public class MealDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        ViewPager viewPager = findViewById(R.id.view_pager);
        ImageAdapter imageAdapter = new ImageAdapter(this); // pass the context to the adapter
        viewPager.setAdapter(imageAdapter);

    }
}
