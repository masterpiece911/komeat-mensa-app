package com.pem.mensa_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.pem.mensa_app.models.meal.Meal;

import java.util.LinkedList;

public class MensaMealListActivity extends AppCompatActivity implements MealListAdapter.MealClickEventListener {
    private MealListModel viewModel;
    private RecyclerView mRecyclerView;
    private MealListAdapter mMealListAdapter;


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

    }

    @Override
    public void onMealClick(int position) {

    }

    @Override
    public void onImageButtonClick(int position) {

    }
}
