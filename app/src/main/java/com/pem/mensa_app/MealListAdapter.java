package com.pem.mensa_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.models.meal.Meal;

import java.util.ArrayList;

public class MealListAdapter extends ListAdapter<Meal, MealListAdapter.MealViewHolder> {

    public static final DiffUtil.ItemCallback<Meal> DIFF_CALLBACK =
           new DiffUtil.ItemCallback<Meal>() {
               @Override
               public boolean areItemsTheSame(@NonNull Meal oldItem, @NonNull Meal newItem) {
                   return oldItem.equals(newItem);
               }

               @Override
               public boolean areContentsTheSame(@NonNull Meal oldItem, @NonNull Meal newItem) {
                   return false;
               }
           };

   public MealListAdapter(){
       super(DIFF_CALLBACK);
   }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_item, parent, false);
        return new MealViewHolder(V);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal currentItem = getItem(position);

        String dishesName = currentItem.getName();
        Double dishesPrice =currentItem.getPrice();

        holder.mTextViewName.setText(dishesName);
        holder.mTextViewPrice.setText("Price: "+dishesPrice);
    }


    public class MealViewHolder extends RecyclerView.ViewHolder{

        public ImageView mImageView;
        public TextView mTextViewName;
        public TextView mTextViewPrice;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView=itemView.findViewById(R.id.image_view);
            mTextViewName=itemView.findViewById(R.id.text_view_dishes);
            mTextViewPrice=itemView.findViewById(R.id.text_view_price);
        }
    }
}
