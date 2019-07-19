package com.pem.mensa_app.ui.home_activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.models.mensa.RestaurantType;

import java.util.List;

import io.opencensus.internal.Utils;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMensaItemSelectedListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

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
        mMap.setInfoWindowAdapter(new MensaMapInfoAdapter());

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
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14.0f));
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        mListener.onMensaSelected((Mensa) marker.getTag());
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
                    .icon(getBitmapDescriptor(type))
                    .position(new LatLng(latitude, longitude))
                    .title(type + " " + name)
            );
            fMarker.setTag(mensa);

        }
    }

    private int getIdforMarker(String type) {
        switch(RestaurantType.fromString(type)){
            case STULOUNGE:
                return R.drawable.ic_lounge_pin;
            case STUBISTRO:
                return R.drawable.ic_bistro_pin;
            case STUCAFE:
                return  R.drawable.ic_cafe_pin;
            case MENSA:
                return  R.drawable.ic_mensa_pin;
            default: return R.drawable.map_pin;
        }
    }

    private BitmapDescriptor getBitmapDescriptor(String type) {
        Drawable vectorDrawable = getContext().getDrawable(getIdforMarker(type));
        int h = vectorDrawable.getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
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
    }

    class MensaMapInfoAdapter implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoContents(Marker marker) {
            Mensa mensa = (Mensa) marker.getTag();
            View border = getLayoutInflater().inflate(R.layout.item_mensa, null);
            TextView name = border.findViewById(R.id.mensa_item_name);
            TextView type = border.findViewById(R.id.mensa_item_type);
            CheckBox box = border.findViewById(R.id.mensa_item_checkbox);
            ImageView arrow = border.findViewById(R.id.mensa_item_arrow);
            box.setVisibility(View.GONE);
            arrow.setVisibility(View.GONE);
            name.setText(mensa.getName());
            type.setText(mensa.getType().toString());
            type.setTextColor(Color.parseColor(mensa.getType().toColor()));
            return border;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }
    }

}
