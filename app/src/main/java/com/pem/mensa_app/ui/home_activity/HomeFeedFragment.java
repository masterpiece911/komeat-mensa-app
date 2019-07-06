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
import com.pem.mensa_app.models.mensa.Mensa;

import java.util.LinkedList;
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

    private OnMensaItemAndCustomizeSelectedListener mListener;
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
        final HomeFeedAdapter adapter = new HomeFeedAdapter(this);
        recyclerView.setAdapter(adapter);
        homeViewModel.getFavoriteMensaList().observe(getViewLifecycleOwner(), new Observer<List<Mensa>>() {
            @Override
            public void onChanged(List<Mensa> mensas) {
                adapter.submitList(new LinkedList<Mensa>(mensas));
                adapter.notifyDataSetChanged();
            }
        });

        MaterialButton button = getView().findViewById(R.id.feed_customize_button);
        button.setOnClickListener(this);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMensaItemAndCustomizeSelectedListener) {
            mListener = (OnMensaItemAndCustomizeSelectedListener) context;
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
    public void mensaClicked(int position) {
        mListener.onMensaSelected(homeViewModel.getMensaList().getValue().get(position));
    }

    @Override
    public void imageClicked(int position) {
        // todo implement images :(
    }
}
