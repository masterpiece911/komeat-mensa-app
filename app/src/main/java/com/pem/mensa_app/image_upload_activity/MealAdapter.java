package com.pem.mensa_app.image_upload_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.R;
import com.pem.mensa_app.models.imageUpoald.MealSelected;
import com.pem.mensa_app.models.meal.Meal;

import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private ArrayList<MealSelected> mDishes;


    public static class MealViewHolder extends RecyclerView.ViewHolder {
        public CheckBox mCheckBox;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mCheckBox = itemView.findViewById(R.id.checkBox_meal_item);
        }
    }

    public MealAdapter(ArrayList<MealSelected> dishes) {
        this.mDishes = dishes;
        //if (dishes == null) {
        //    this.mDishes = new ArrayList<>();
        //    MealSelected mealSelected = new MealSelected("Test", false);
        //    mDishes.add(mealSelected);
        //} else if (dishes.isEmpty()) {
        //    this.mDishes = dishes;
        //    MealSelected mealSelected = new MealSelected("Test", false);
        //    mDishes.add(mealSelected);
        //} else {
        //    this.mDishes = dishes;
        //}
    }

    public void setMealData(ArrayList<MealSelected> dishes) {
        this.mDishes = dishes;
        this.notifyDataSetChanged();

    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meal_item_image_upload, parent, false);
        MealViewHolder mealViewHolder = new MealViewHolder(view);
        return mealViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        MealSelected mealSelected = mDishes.get(position);
        holder.mCheckBox.setText(mealSelected.getmDescription());
    }

    @Override
    public int getItemCount() {
        return mDishes.size();
    }
}
