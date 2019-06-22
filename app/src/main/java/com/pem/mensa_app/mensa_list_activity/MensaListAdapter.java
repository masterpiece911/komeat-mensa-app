package com.pem.mensa_app.mensa_list_activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.R;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.models.mensa.VisibilityPreference;

public class MensaListAdapter extends ListAdapter<Mensa, MensaListAdapter.MensaViewHolder> {

    private ItemListener mItemListener;
    private final Context context;

    class MensaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameLabel;
        TextView addressLabel;
        TextView restaurantTypeLabel;
        TextView occupancyLabel;
        TextView distanceLabel;
        ImageButton favoriteButton;
        ImageButton hideButton;
        ItemListener fItemListener;


        public MensaViewHolder(@NonNull View itemView, ItemListener itemListener) {
            super(itemView);

            fItemListener = itemListener;

            nameLabel = itemView.findViewById(R.id.name_label);
            addressLabel = itemView.findViewById(R.id.address_label);
            restaurantTypeLabel = itemView.findViewById(R.id.restauranttype_label);
            occupancyLabel = itemView.findViewById(R.id.occupancy_label);
            distanceLabel = itemView.findViewById(R.id.distance_label);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            hideButton = itemView.findViewById(R.id.hide_button);

            itemView.setOnClickListener(this);

        }

        public void bindData(final Mensa newMensa) {

            nameLabel.setText(newMensa.getName());
            addressLabel.setText(newMensa.getAddress());
            restaurantTypeLabel.setText(newMensa.getType().toString());
            String occText = "Occupancy: " + newMensa.getOccupancy().toInt();
            occupancyLabel.setText(occText);
            if (newMensa.getDistance() != -1) {
                distanceLabel.setVisibility(View.VISIBLE);
                distanceLabel.setText(Double.toString(newMensa.getDistance()));
            } else {
                distanceLabel.setVisibility(View.INVISIBLE);
            }

            switch(newMensa.getVisibility()){
                case FAVORITE:
                    favoriteButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.favorite_active, null));
                    favoriteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fItemListener.visibilityButtonClicked(newMensa, VisibilityPreference.DEFAULT);
                        }
                    });
                    hideButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.hidden_inactive, null));
                    hideButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fItemListener.visibilityButtonClicked(newMensa, VisibilityPreference.HIDDEN);
                        }
                    }); break;
                case DEFAULT:
                    favoriteButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.favorite_inactive, null));
                    favoriteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fItemListener.visibilityButtonClicked(newMensa, VisibilityPreference.FAVORITE);
                        }
                    });
                    hideButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.hidden_inactive, null));
                    hideButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fItemListener.visibilityButtonClicked(newMensa, VisibilityPreference.HIDDEN);
                        }
                    }); break;
                case HIDDEN:
                    favoriteButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.favorite_inactive, null));
                    favoriteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fItemListener.visibilityButtonClicked(newMensa, VisibilityPreference.FAVORITE);
                        }
                    });
                    hideButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.hidden_active, null));
                    hideButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fItemListener.visibilityButtonClicked(newMensa, VisibilityPreference.DEFAULT);
                        }
                    }); break;


            }

        }

        @Override
        public void onClick(View v) {
            fItemListener.elementClicked(getAdapterPosition());
        }
    }

    public MensaListAdapter(Context context, ItemListener mItemListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.mItemListener = mItemListener;
    }

    private static final DiffUtil.ItemCallback<Mensa> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Mensa>() {
                @Override
                public boolean areItemsTheSame(@NonNull Mensa oldItem, @NonNull Mensa newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Mensa oldItem, @NonNull Mensa newItem) {
                    return oldItem.getDistance() == newItem.getDistance()
                            && oldItem.getOccupancy().equals(newItem.getOccupancy())
                            && oldItem.getVisibility().equals(newItem.getVisibility());
                }
            };

    @Override
    public int getItemViewType(int position) {
        return R.layout.mensa_list_item;
    }

    @NonNull
    @Override
    public MensaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new MensaViewHolder(view, mItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MensaViewHolder holder, int position) {
        holder.bindData(getItem(position));
    }

    public interface ItemListener {
        void visibilityButtonClicked(Mensa mensa, VisibilityPreference newVisibility);
        void elementClicked(int position);
    }

}
