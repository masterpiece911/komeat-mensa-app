package com.pem.mensa_app.ui.home_activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pem.mensa_app.R;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MensaListFragment extends Fragment implements MensaListAdapter.MensaClickListener {

    private static final String HAS_CHECKBOX = "section_has_checkbox";
    private static final String HAS_ARROW = "section_has_arrow";

    private boolean CHECKBOX_ENABLED;
    private boolean SHOW_ARROW;



    private HomeViewModel homeViewModel;
    private OnMensaItemSelectedListener callback;

    public static MensaListFragment newInstance(boolean hasCheckbox, boolean hasArrow) {
        MensaListFragment fragment = new MensaListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(HAS_CHECKBOX, hasCheckbox);
        bundle.putBoolean(HAS_ARROW, hasArrow);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean hasArrow = false, hasCheckbox = false;
        if (getArguments() != null) {
            hasArrow = getArguments().getBoolean(HAS_ARROW);
            hasCheckbox = getArguments().getBoolean(HAS_CHECKBOX);
        }

        CHECKBOX_ENABLED = hasCheckbox;
        SHOW_ARROW = hasArrow;

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mensalist, container, false);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);

        final RecyclerView recyclerView = getView().findViewById(R.id.mensa_list_frag_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 1));
        final MensaListAdapter adapter = new MensaListAdapter(this, SHOW_ARROW, CHECKBOX_ENABLED);
        recyclerView.setAdapter(adapter);

        homeViewModel.getMensaList().observe(getViewLifecycleOwner(), new Observer<List<Mensa>>() {
            @Override
            public void onChanged(List<Mensa> mensas) {
                adapter.submitList(new ArrayList<>(mensas));
                adapter.notifyDataSetChanged();
            }
        });

        if (CHECKBOX_ENABLED) {
            homeViewModel.getFavoriteMensaList().observe(getViewLifecycleOwner(), new Observer<List<Mensa>>() {
                @Override
                public void onChanged(List<Mensa> mensas) {
                    adapter.setFavorites(new LinkedList<>(mensas));
                }
            });
        }

    }

    @Override
    public void itemClicked(Mensa selectedMensa) {
        if(CHECKBOX_ENABLED) {
            homeViewModel.flipFavoriteMensa(selectedMensa);
        } else {
            this.callback.onMensaSelected(selectedMensa);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMensaItemSelectedListener) {
            callback = (OnMensaItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMensaItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
