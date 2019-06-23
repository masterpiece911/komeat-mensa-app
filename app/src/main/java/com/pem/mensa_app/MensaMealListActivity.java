package com.pem.mensa_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pem.mensa_app.models.meal.Meal;

import java.util.LinkedList;

public class MensaMealListActivity extends AppCompatActivity implements MealListAdapter.MealClickEventListener {
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
        viewModel.setMensaName(extras.getString(getString(R.string.intent_mensa_name)));
        viewModel.setMensaID(extras.getString(getString(R.string.intent_mensa_uid)));
        viewModel.setMealPlanReferencePath(extras.getString(getString(R.string.intent_mensa_meal_plan_reference_path)));
        viewModel.setMensaEatApiUrl(extras.getString(getString(R.string.intent_mensa_eatapi_url)));

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
            case 0: rBtoCheck = R.id.radioButton; break;
            case 1: rBtoCheck = R.id.radioButton2; break;
            case 2: rBtoCheck = R.id.radioButton3; break;
            case 3: rBtoCheck = R.id.radioButton4; break;
            case 4: rBtoCheck = R.id.radioButton5; break;
        }
        mRadioGroup.check(rBtoCheck);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton clickedButton = findViewById(i);
                int weekday = 0;
                switch(clickedButton.getText().toString()) {
                    case "Mo": weekday = 0; break;
                    case "Tue": weekday = 1; break;
                    case "Wed": weekday = 2; break;
                    case "Thu": weekday = 3; break;
                    case "Fr": weekday = 4; break;
                }
                viewModel.setSelectedWeekday(weekday);
            }
        });

    }

    @Override
    public void onMealClick(int position) {

    }

    @Override
    public void onImageButtonClick(int position) {

    }
}
