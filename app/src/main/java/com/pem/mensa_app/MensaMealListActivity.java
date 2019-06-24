package com.pem.mensa_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pem.mensa_app.meal_detail_activity.MealDetailActivity;
import com.pem.mensa_app.models.meal.Meal;

import java.util.LinkedList;

public class MensaMealListActivity extends AppCompatActivity implements MealListAdapter.MealClickEventListener {

    private static final String TAG = MensaMealListActivity.class.getSimpleName();

    private MealListModel viewModel;
    private RecyclerView mRecyclerView;
    private MealListAdapter mMealListAdapter;
    private RadioGroup mRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensa_meal_list);

        Bundle extras = getIntent().getExtras();

        viewModel = ViewModelProviders.of(this).get(MealListModel.class);
        viewModel.setData(extras);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mMealListAdapter = new MealListAdapter(this);
        mRecyclerView.setAdapter(mMealListAdapter);

        viewModel.getMealData().observe(this, new Observer<LinkedList<Meal>>() {
            @Override
            public void onChanged(LinkedList<Meal> meals) {
                mMealListAdapter.submitList(new LinkedList<>(meals));
                mMealListAdapter.notifyDataSetChanged();
            }
        });

        viewModel.informationSet();

        mRadioGroup = findViewById(R.id.meal_list_control);
        int rBtoCheck = 0;
        switch(viewModel.getSelectedWeekday()){
            case 1: rBtoCheck = R.id.radioButton; break;
            case 2: rBtoCheck = R.id.radioButton2; break;
            case 3: rBtoCheck = R.id.radioButton3; break;
            case 4: rBtoCheck = R.id.radioButton4; break;
            case 5: rBtoCheck = R.id.radioButton5; break;
        }
        mRadioGroup.check(rBtoCheck);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton clickedButton = findViewById(i);
                int weekday = 0;
                switch(clickedButton.getText().toString()) {
                    case "Mo": weekday = 1; break;
                    case "Tue": weekday = 2; break;
                    case "Wed": weekday = 3; break;
                    case "Thu": weekday = 4; break;
                    case "Fr": weekday = 5; break;
                }
                viewModel.setSelectedWeekday(weekday);
            }
        });

    }

    @Override
    public void onMealClick(int position) {

        Meal clickedMeal = viewModel.getMealData().getValue().get(position);

        Intent mealDetailIntent = new Intent(MensaMealListActivity.this, MealDetailActivity.class);
        mealDetailIntent.putExtra(getString(R.string.intent_meal_uid), clickedMeal.getUid());
        startActivity(mealDetailIntent);

    }

    @Override
    public void onImageButtonClick(int position) {
        Log.d(TAG, "onImageButtonClick: " + position);
    }
}
