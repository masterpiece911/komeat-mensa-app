package com.pem.mensa_app.ui.image_upload_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.R;
import com.pem.mensa_app.models.imageUpoald.MealSelected;

import java.util.ArrayList;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private ArrayList<MealSelected> mDishes;
    private MealClickedListener mListener;


    public static class MealViewHolder extends RecyclerView.ViewHolder {
        public CheckBox mCheckBox;
        private MealClickedListener mListener;

        public MealViewHolder(@NonNull View itemView, MealClickedListener listener) {
            super(itemView);
            mCheckBox = itemView.findViewById(R.id.checkBox_meal_item);
            this.mListener = listener;
        }

        public void bindView(MealSelected mealselected) {
            final MealSelected mealSelected = mealselected;
            mCheckBox.setText(mealSelected.getmDescription());
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mListener.mealClicked(mealSelected, isChecked);
                }
            });
        }
    }

    public MealAdapter(ArrayList<MealSelected> dishes, MealClickedListener listener) {
        this.mDishes = dishes;
        this.mListener = listener;
    }

    public void setMealData(ArrayList<MealSelected> dishes) {
        this.mDishes = dishes;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meal_item_image_upload, parent, false);
        MealViewHolder mealViewHolder = new MealViewHolder(view, mListener);
        return mealViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        final MealSelected mealSelected = mDishes.get(position);
        holder.bindView(mealSelected);
    }

    @Override
    public int getItemCount() {
        return mDishes.size();
    }

    interface MealClickedListener {
        void mealClicked(MealSelected meal, boolean selected);
    }
}
