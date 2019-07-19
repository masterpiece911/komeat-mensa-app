package com.pem.mensa_app.ui.home_activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.R;
import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.models.mensa.MensaDay;

import java.util.ArrayList;

public class HomeFeedAdapter extends ListAdapter<MensaDay, HomeFeedAdapter.MensaViewHolder> {

    private final EdgeDecorator DECORATOR;


    private final MensaDetailClickListener listener;
    private final RecyclerView.RecycledViewPool viewPool;
    private Context context;
    private int corner_radius;

    public HomeFeedAdapter(Context context, MensaDetailClickListener listener, int corner_radius, int edge_padding, int padding) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
        this.viewPool = new RecyclerView.RecycledViewPool();
        this.corner_radius = corner_radius;

        DECORATOR = new EdgeDecorator(edge_padding, padding);

    }

    public static final DiffUtil.ItemCallback<MensaDay> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MensaDay>() {
                @Override
                public boolean areItemsTheSame(@NonNull MensaDay oldItem, @NonNull MensaDay newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull MensaDay oldItem, @NonNull MensaDay newItem) {
                    return oldItem.getMeals().equals(newItem.getMeals());
                }
            };

    @NonNull
    @Override
    public MensaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensa_expanded, parent, false);
        return new MensaViewHolder(V, listener, viewPool);
    }

    @Override
    public void onBindViewHolder(@NonNull MensaViewHolder holder, int position) {
        MensaDay currentItem = getItem(position);

        holder.bindData(currentItem);
    }

    class MensaViewHolder extends RecyclerView.ViewHolder {

        private View mItemView;
        private TextView mTextName;
        private TextView mTextType;
        private RecyclerView mRecyclerView;
        private HomeFeedImageAdapter mAdapter;
        MensaDetailClickListener mListener;

        public MensaViewHolder(@NonNull View itemView, MensaDetailClickListener listener, RecyclerView.RecycledViewPool viewPool) {
            super(itemView);
            mListener = listener;
            mItemView = itemView;
            mTextName = itemView.findViewById(R.id.mensa_item_expanded_name);
            mTextType = itemView.findViewById(R.id.mensa_item_expanded_type);
            mRecyclerView = itemView.findViewById(R.id.mensa_item_expanded_image_recyclerview);
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, 1, RecyclerView.HORIZONTAL, false));
            mRecyclerView.setRecycledViewPool(viewPool);
            mAdapter = new HomeFeedImageAdapter(listener, corner_radius);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.addItemDecoration(DECORATOR);
        }

        public void bindData(final MensaDay data) {
            final Mensa m = data.getMensa();
            mAdapter.setMensa(data.getMensa());
            mAdapter.submitList(new ArrayList<>(data.getMeals()));
            mTextType.setText(m.getType().toString());
            mTextName.setText(m.getName());

            int color = Color.parseColor(m.getType().toColor());

            mTextType.setTextColor(color);

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.mensaClicked(m);
                }
            });

        }

    }

    interface MensaDetailClickListener {
        void mensaClicked(Mensa mensa);
        void imageClicked(Mensa mensa, Meal meal);
    }

}
