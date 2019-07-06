package com.pem.mensa_app;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.models.mensa.RestaurantType;
import com.pem.mensa_app.ui.home_activity.HomeViewModel;
import com.pem.mensa_app.ui.home_activity.OnMensaItemSelectedListener;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMensaItemSelectedListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private HomeViewModel homeViewModel;
    private GoogleMap mMap;

    private OnMensaItemSelectedListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        homeViewModel.getMensaList().observe(getViewLifecycleOwner(), new Observer<List<Mensa>>() {
            @Override
            public void onChanged(List<Mensa> mensas) {
                drawMarkers(mensas);
            }
        });

        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng position = new LatLng(48.1351253,11.5819806);
//        mMap.addMarker(new MarkerOptions().position(position).title("Your selected location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 11.0f));
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14.0f));
        return false;
    }

    private void drawMarkers(List<Mensa> mensas) {
        Marker fMarker;
        double latitude, longitude;
        String name, type;
        for (Mensa mensa : mensas) {
            name = mensa.getName();
            type = mensa.getType().toString();
            latitude = mensa.getLatitude();
            longitude = mensa.getLongitude();

            fMarker = mMap.addMarker(new MarkerOptions()
                .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(getHue(type)))
                    .position(new LatLng(latitude, longitude))
                    .title(type + " " + name)
            );
            fMarker.setTag(mensa);

        }
    }

    private int getHue(String type) {
        switch(RestaurantType.fromString(type)){
            case STULOUNGE:
                return 30;
            case STUBISTRO:
                return 338;
            case STUCAFE:
                return 34;
            case MENSA:
                return 90;
            default: return 0;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMensaItemSelectedListener) {
            mListener = (OnMensaItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMensaItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }}
