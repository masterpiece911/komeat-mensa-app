package com.pem.mensa_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class MensaMealListActivity extends AppCompatActivity {
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

        viewModel.informationSet();

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mMealListAdapter = new MealListAdapter();
        mRecyclerView.setAdapter(mMealListAdapter);

//        parseJSON();

    }

}
