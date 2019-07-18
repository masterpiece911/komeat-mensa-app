package com.pem.mensa_app.ui.home_activity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.pem.mensa_app.models.mensa.Mensa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class HomeFeedImageAdapter extends ListAdapter<Meal, HomeFeedImageAdapter.ImageViewHolder> {

    private final HomeFeedAdapter.MensaDetailClickListener mListener;
    private Mensa mensa;
    private Random random = new Random();

    public HomeFeedImageAdapter(HomeFeedAdapter.MensaDetailClickListener listener) {
        super(DIFF_CALLBACK);
        mListener = listener;
    }

    public void setMensa(Mensa mensa) {
        this.mensa = mensa;
    }

    public static final DiffUtil.ItemCallback<Meal> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Meal>() {
                @Override
                public boolean areItemsTheSame(@NonNull Meal oldItem, @NonNull Meal newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Meal oldItem, @NonNull Meal newItem) {
                    if(oldItem.getImages() == null) {
                        return newItem.getImages() != null;
                    } else {
                        return oldItem.getImages().equals(newItem.getImages());
                    }
                }
            };

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensa_expanded_subitem, parent, false);
        return new ImageViewHolder(V, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Meal currentItem = getItem(position);

        holder.bindData(currentItem);
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        private View mItemView;
        private TextView mMealName;
        private ImageView mMealImage;
        private HomeFeedAdapter.MensaDetailClickListener mListener;

        public ImageViewHolder(@NonNull View itemView, HomeFeedAdapter.MensaDetailClickListener listener) {
            super(itemView);
//            mListener = listener;
            mItemView = itemView;
            mMealName = itemView.findViewById(R.id.item_mensa_expanded_subitem_meal_name);
            mMealImage = itemView.findViewById(R.id.item_mensa_expanded_subitem_image);
            mListener = listener;
        }

        public void bindData(final Meal data) {
            mMealName.setText(data.getName());
//            Log.d("imagelistbinder", String.format("Meal %s, id: %s, images: %s", data.getName(), data.getUid(), data.getImages()));
            if (data.getImages().equals(new ArrayList<String>())) {
                GlideApp.with(itemView)
                        .load(R.drawable.placeholder)
                        .into(mMealImage);
            } else {

                String path = data.getImages().get(random.nextInt(data.getImages().size()));
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("images/" + path);
                GlideApp.with(itemView)
                        .load(storageRef)
                        .placeholder(R.drawable.placeholder)
                        .into(mMealImage);
            }

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.imageClicked(mensa, data);
                }
            });
        }

    }
}
