package com.pem.mensa_app.ui.home_activity;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.R;
import com.pem.mensa_app.models.mensa.Mensa;

public class HomeFeedAdapter extends ListAdapter<Mensa, HomeFeedAdapter.MensaViewHolder> {

    private static final String MENSA = "#76AD40";
    private static final String STUBISTRO = "#B90748";
    private static final String STUCAFE = "#F18800";
    private static final String STULOUNGE = "#5D2F00";

    private final MensaDetailClickListener listener;

    public HomeFeedAdapter(MensaDetailClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
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
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensa_expanded, parent, false);
        return new MensaViewHolder(V, listener);
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
        MensaDetailClickListener mListener;

        public MensaViewHolder(@NonNull View itemView, MensaDetailClickListener listener) {
            super(itemView);
            mListener = listener;
            mItemView = itemView;
            mTextName = itemView.findViewById(R.id.mensa_item_expanded_name);
            mTextType = itemView.findViewById(R.id.mensa_item_expanded_type);
        }

        public void bindData(Mensa data) {
            mTextType.setText(data.getType().toString());
            mTextName.setText(data.getName());

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

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.mensaClicked(getAdapterPosition());
                }
            });

        }

    }

    interface MensaDetailClickListener {
        void mensaClicked(int position);
        void imageClicked(int position);
    }

}
