package com.pem.mensa_app.ui.meal_list;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.mensa.RestaurantType;
import com.pem.mensa_app.ui.image_upload_activity.ImageUploadActivity;
import com.pem.mensa_app.ui.meal_detail_activity.MealDetailActivity;
import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.viewmodels.MealListModel;

import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.LinkedList;

public class MensaMealListActivity extends AppCompatActivity implements MealListAdapter.MealClickEventListener {

    private static final String TAG = MensaMealListActivity.class.getSimpleName();

    private MealListModel viewModel;
    private RecyclerView mRecyclerView;
    private MealListAdapter mMealListAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensa_meal_list);

        Bundle extras = getIntent().getExtras();

        viewModel = ViewModelProviders.of(this).get(MealListModel.class);
        viewModel.setData(extras);

        mRecyclerView = findViewById(R.id.meal_list_recycler_view);
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

        Toolbar toolbar = findViewById(R.id.meal_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        View title = getLayoutInflater().inflate(R.layout.item_mensa, toolbar);
//        TextView textViewTitle = title.findViewById(R.id.mensa_item_name);
//        TextView textViewType = title.findViewById(R.id.mensa_item_type);
//        CheckBox box = title.findViewById(R.id.mensa_item_checkbox);
//        ImageView arrow = title.findViewById(R.id.mensa_item_arrow);
//        box.setVisibility(View.GONE);
//        arrow.setVisibility(View.GONE);
//        textViewTitle.setText(extras.getString(getString(R.string.intent_mensa_name)));
//        RestaurantType type = RestaurantType.fromString(extras.getString(getString(R.string.intent_mensa_type)));
//        textViewType.setText(type.toString());
//        textViewType.setTextColor(Color.parseColor(type.toColor()));

        TextView textViewTitle = findViewById(R.id.meal_list_mensa_name);
        TextView textViewType = findViewById(R.id.meal_list_mensa_type);
        textViewTitle.setText(extras.getString(getString(R.string.intent_mensa_name)));
        RestaurantType type = RestaurantType.fromString(extras.getString(getString(R.string.intent_mensa_type)));
        textViewType.setText(type.toString());
        textViewType.setTextColor(Color.parseColor(type.toColor()));


        LocalDate date = new LocalDate(DateTimeZone.forID("Europe/Berlin"));
        LocalDate today = new LocalDate(date);
        if (today.getDayOfWeek() > 5) {
            today = today.withField(DateTimeFieldType.dayOfWeek(), 5);
        }
        boolean selected = false;
        String full, compact;

        mTabLayout = findViewById(R.id.meal_list_tabs);
        for (int i = 0; i < 5; i++) {
            TabLayout.Tab tab = mTabLayout.newTab();
            date = date.withField(DateTimeFieldType.dayOfWeek(), i+1);
            full = date.toString("EEE\n dd.MM");
            compact = date.toString("EEE");
            selected = date.compareTo(today) == 0;
            mTabLayout.addTab(tab, i, selected);
            tab.setText(selected ? full : compact);
            tab.setTag(new Pair<>(full, compact));
        }
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.setSelectedWeekday(tab.getPosition() + 1);
                Pair<String, String> tag = (Pair<String, String>) tab.getTag();
                tab.setText(tag.first);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Pair<String, String> tag = (Pair<String, String>) tab.getTag();
                tab.setText(tag.second);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // do nothing
            }
        });

    }

    @Override
    public void onMealClick(Meal clickedMeal) {

        Intent mealDetailIntent = new Intent(MensaMealListActivity.this, MealDetailActivity.class);
        mealDetailIntent.putExtra(getString(R.string.intent_meal_uid), clickedMeal.getUid());
        mealDetailIntent.putExtra("meal_path", viewModel.getMealPlanReferencePath());
        mealDetailIntent.putExtra("day", viewModel.getSelectedWeekday());

        startActivity(mealDetailIntent);
    }

    @Override
    public void onImageButtonClick(Meal clickedMeal) {
        Log.d(TAG, "onImageButtonClick: " + clickedMeal.getName());

        Intent imageUploadActivityIntent = new Intent(MensaMealListActivity.this, ImageUploadActivity.class);

        imageUploadActivityIntent.putExtra("meal_uid", clickedMeal.getUid());
        imageUploadActivityIntent.putExtra("meal_path", viewModel.getMealPlanReferencePath());
        imageUploadActivityIntent.putExtra("day", viewModel.getSelectedWeekday());
        startActivity(imageUploadActivityIntent);
    }
}
