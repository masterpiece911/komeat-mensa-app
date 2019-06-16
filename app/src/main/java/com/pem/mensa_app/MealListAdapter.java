package com.pem.mensa_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.models.meal.Meal;

import java.util.ArrayList;

public class MealListAdapter extends RecyclerView.Adapter<MealListAdapter.MealVieHolder> {

   private Context mContext;
   private ArrayList<Meal> mMealList;

   public MealListAdapter(Context context, ArrayList<Meal> mealList){
       mContext=context;
       mMealList=mealList;

   }

    @NonNull
    @Override
    public MealVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(mContext).inflate(R.layout.example_item, parent, false);
        return new MealVieHolder(V);
    }

    @Override
    public void onBindViewHolder(@NonNull MealVieHolder holder, int position) {
        Meal currentItem = mMealList.get(position);

        String dishesName = currentItem.getName();
        Float dishesPrice =currentItem.getPrice();

        holder.mTextViewName.setText(dishesName);
        holder.mTextViewPrice.setText("Price: "+dishesPrice);
    }

    @Override
    public int getItemCount() {
        return mMealList.size();
    }


    public class MealVieHolder extends RecyclerView.ViewHolder{

        public ImageView mImageView;
        public TextView mTextViewName;
        public TextView mTextViewPrice;

        public MealVieHolder(@NonNull View itemView) {
            super(itemView);
            mImageView=itemView.findViewById(R.id.image_view);
            mTextViewName=itemView.findViewById(R.id.text_view_dishes);
            mTextViewPrice=itemView.findViewById(R.id.text_view_price);
        }
    }
}
