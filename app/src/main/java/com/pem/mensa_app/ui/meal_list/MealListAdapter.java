package com.pem.mensa_app.ui.meal_list;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pem.mensa_app.GlideApp;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.utilities.eatapi.MealIngredientParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MealListAdapter extends ListAdapter<Meal, MealListAdapter.MealViewHolder> {

    private final MealClickEventListener listener;

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

    public MealListAdapter(MealClickEventListener listener) {
       super(DIFF_CALLBACK);
       this.listener = listener;
   }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_list, parent, false);
        return new MealViewHolder(V, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal currentItem = getItem(position);

        holder.bindData(currentItem);
    }


    public class MealViewHolder extends RecyclerView.ViewHolder{

        private View mItemView;
        private ImageView mImageView;
        private TextView mTextViewName;
        private TextView mTextViewIngredients;
        private Button mButtonImageUpload;
        MealClickEventListener mClickListener;

        public MealViewHolder(@NonNull View itemView, MealClickEventListener listener) {
            super(itemView);
            mItemView = itemView;
            mClickListener = listener;
            mImageView=itemView.findViewById(R.id.image_view);
            mTextViewName=itemView.findViewById(R.id.text_view_dishes);
            mTextViewIngredients= itemView.findViewById(R.id.text_view_ingredients);
            mButtonImageUpload = itemView.findViewById(R.id.button_take_image);
        }

        public void bindData(final Meal data) {
            mTextViewName.setText(data.getName());

            if (data.getUid() == null) {
                mImageView.setVisibility(View.GONE);
                mTextViewIngredients.setVisibility(View.GONE);
                mButtonImageUpload.setVisibility(View.GONE);
                mButtonImageUpload.setOnClickListener(null);
                mItemView.setOnClickListener(null);
                return;
            } else {
                mImageView.setVisibility(View.VISIBLE);
                mTextViewIngredients.setVisibility(View.VISIBLE);
                mButtonImageUpload.setVisibility(View.VISIBLE);
            }

            List<String> ingredientIds = new LinkedList<>();
            // TODO: Refactor
            for (String i : data.getIngredients()) {
                ingredientIds.add(i);
            }
            Log.d("mealadapter", data.getName() + ingredientIds.toString());
            Pair<String, Integer> pillInfo = MealIngredientParser.getPillFromIngredients(ingredientIds);
            mTextViewIngredients.setText(pillInfo.first);
            mTextViewIngredients.getBackground().setTint(pillInfo.second);

            mButtonImageUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.onImageButtonClick(data);
                }
            });

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onMealClick(data);
                }
            });
             ArrayList<String> imagelist= data.getImages();
             if (imagelist==null || imagelist.isEmpty()){
                 mImageView.setVisibility(View.GONE);
//                 mImageView.setImageResource(R.drawable.placeholder);
             }else {
                 mImageView.setVisibility(View.VISIBLE);
                 StorageReference reference = FirebaseStorage.getInstance().getReference("/images/"+imagelist.get(0));
                 GlideApp.with(this.mItemView).load(reference).into(mImageView);
             }

        }

    }


    interface MealClickEventListener {
       void onMealClick(Meal meal);
       void onImageButtonClick(Meal meal);
    }
}
