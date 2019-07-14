package com.pem.mensa_app.ui.home_activity;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.R;
import com.pem.mensa_app.models.mensa.Mensa;

import java.util.LinkedList;

public class MensaListAdapter extends ListAdapter<Mensa, MensaListAdapter.MensaViewHolder> {

    private static final String MENSA = "#76AD40";
    private static final String STUBISTRO = "#B90748";
    private static final String STUCAFE = "#F18800";
    private static final String STULOUNGE = "#5D2F00";

    private final boolean hasArrow;
    private final boolean hasCheckbox;

    private final MensaClickListener listener;

    private LinkedList<Mensa> favorites = new LinkedList<>();

    interface MensaClickListener {
        void itemClicked(Mensa mensa);
    }

    public MensaListAdapter(MensaClickListener listener, boolean hasArrow, boolean hasCheckbox) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.hasArrow = hasArrow;
        this.hasCheckbox = hasCheckbox;
    }

    public void setFavorites (LinkedList<Mensa> favorites) {
        this.favorites = favorites;
        this.notifyDataSetChanged();
    }

    public static final DiffUtil.ItemCallback<Mensa> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Mensa>() {
                @Override
                public boolean areItemsTheSame(@NonNull Mensa oldItem, @NonNull Mensa newItem) {
                    return oldItem.getuID().equals(newItem.getuID());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Mensa oldItem, @NonNull Mensa newItem) {
                    return false;
                }
            };

    @NonNull
    @Override
    public MensaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensa, parent, false);
        return new MensaViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MensaViewHolder holder, int position) {
        Mensa currentItem = getItem(position);

        holder.bindData(currentItem);
    }

    class MensaViewHolder extends RecyclerView.ViewHolder {

        private View mItemView;
        private TextView mTextName;
        private TextView mTextType;
        private CheckBox mCheckBox;
        private ImageView mArrow;
        MensaClickListener mensaClickListener;

        public MensaViewHolder(@NonNull View itemView, MensaClickListener listener) {

            super(itemView);
            mensaClickListener = listener;
            mItemView = itemView;
            mTextName = itemView.findViewById(R.id.mensa_item_name);
            mTextType = itemView.findViewById(R.id.mensa_item_type);
            mCheckBox = itemView.findViewById(R.id.mensa_item_checkbox);
            mArrow = itemView.findViewById(R.id.mensa_item_arrow);

        }

        public void bindData(final Mensa data) {
            mTextName.setText(data.getName());
            mTextType.setText(data.getType().toString());

            int color = 0x000000;

            switch (data.getType()) {
                case MENSA:
                    color = Color.parseColor(MENSA);
                    break;
                case STUCAFE:
                    color = Color.parseColor(STUCAFE);
                    break;
                case STUBISTRO:
                    color = Color.parseColor(STUBISTRO);
                    break;
                case STULOUNGE:
                    color = Color.parseColor(STULOUNGE);
                    break;
            }

            mTextType.setTextColor(color);

            if (!hasArrow) {
                mArrow.setVisibility(View.GONE);
            } else {
                mArrow.setVisibility(View.VISIBLE);
            }
            if (!hasCheckbox) {
                mCheckBox.setVisibility(View.GONE);
            } else {
                mCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mensaClickListener.itemClicked(data);
                    }
                });
                mCheckBox.setVisibility(View.VISIBLE);
                if (favorites.contains(data)) {
                    mCheckBox.setChecked(true);
                } else {
                    mCheckBox.setChecked(false);
                }
            }



            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCheckBox.toggle();
                    mensaClickListener.itemClicked(data);
                }
            });
        }

    }



}
