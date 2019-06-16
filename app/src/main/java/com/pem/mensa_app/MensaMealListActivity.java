package com.pem.mensa_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.pem.mensa_app.models.meal.Meal;

import java.util.ArrayList;

public class MensaMealListActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private MealListAdapter mMealListAdapter;
    private ArrayList<Meal> mMealList;
    private RequestQueue mRequestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensa_meal_list);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        
    }
}
