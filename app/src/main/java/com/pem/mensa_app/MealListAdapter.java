package com.pem.mensa_app;

import android.text.TextUtils;
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
import com.pem.mensa_app.models.meal.Ingredient;
import com.pem.mensa_app.models.meal.Meal;

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
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_item, parent, false);
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
        private TextView mTextViewPrice;
        private TextView mTextViewIngredients;
        private Button mButtonImageUpload;
        MealClickEventListener mClickListener;

        public MealViewHolder(@NonNull View itemView, MealClickEventListener listener) {
            super(itemView);
            mItemView = itemView;
            mClickListener = listener;
            mImageView=itemView.findViewById(R.id.image_view);
            mTextViewName=itemView.findViewById(R.id.text_view_dishes);
            mTextViewPrice=itemView.findViewById(R.id.text_view_price);
            mTextViewIngredients= itemView.findViewById(R.id.text_view_ingredients);
            mButtonImageUpload = itemView.findViewById(R.id.button_take_image);
        }

        public void bindData(Meal data) {
            mTextViewName.setText(data.getName());
            mTextViewPrice.setText("Price: " + data.getPrice());
            List<String> ingredientIds = new LinkedList<>();
            for (Ingredient i : data.getIngredients()) {
                ingredientIds.add(i.getId());
            }
            String ingredientString = TextUtils.join(", ", ingredientIds);
            mTextViewIngredients.setText(ingredientString);

            mButtonImageUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.onImageButtonClick(getAdapterPosition());
                }
            });

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onMealClick(getAdapterPosition());
                }
            });

            StorageReference reference = FirebaseStorage.getInstance().getReference("/images/halbeshendl.png");
            GlideApp.with(this.mItemView).load(reference).into(mImageView);
        }

    }

    interface MealClickEventListener {
       void onMealClick(int position);
       void onImageButtonClick(int position);
    }
}
