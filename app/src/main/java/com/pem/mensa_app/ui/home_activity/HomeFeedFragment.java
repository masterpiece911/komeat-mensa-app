package com.pem.mensa_app.ui.home_activity;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.models.mensa.MensaDay;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMensaItemSelectedListener} interface
 * to handle interaction events.
 * Use the {@link HomeFeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFeedFragment extends Fragment implements HomeFeedAdapter.MensaDetailClickListener, View.OnClickListener {

    private static final String TAG = HomeFeedFragment.class.getSimpleName();

    private HomeFeedItemsListener mListener;
    private HomeViewModel homeViewModel;

    public HomeFeedFragment() {
        // Required empty public constructor
    }

    public static HomeFeedFragment newInstance() {
        HomeFeedFragment fragment = new HomeFeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_feed, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        RecyclerView recyclerView = getView().findViewById(R.id.feed_frag_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        int corner_radius = getContext().getResources().getDimensionPixelSize(R.dimen.corner_radius_homefeed);
        int edge_padding = getContext().getResources().getDimensionPixelSize(R.dimen.edge_padding_homefeed);
        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.padding_homefeed);

        final HomeFeedAdapter adapter = new HomeFeedAdapter(getContext(), this, corner_radius, edge_padding, padding);
        recyclerView.setAdapter(adapter);

        MaterialButton button = getView().findViewById(R.id.feed_customize_button);
        button.setOnClickListener(this);

        homeViewModel.getFavoriteMensaDetails().observe(getViewLifecycleOwner(), new Observer<List<MensaDay>>() {
            @Override
            public void onChanged(List<MensaDay> mensaDays) {
                adapter.submitList(new ArrayList<>(mensaDays));
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeFeedItemsListener) {
            mListener = (HomeFeedItemsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMensaItemSelectedListener");
        }
    }

    @Override
    public void onClick(View v) {
        mListener.onCustomizeClicked();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void mensaClicked(Mensa mensa) {
        mListener.onMensaSelected(mensa);
    }

    @Override
    public void imageClicked(Mensa mensa, Meal meal) {
        mListener.onImageClicked(mensa, meal);
    }
}
