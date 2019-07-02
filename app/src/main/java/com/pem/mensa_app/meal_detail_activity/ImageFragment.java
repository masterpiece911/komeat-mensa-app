package com.pem.mensa_app.meal_detail_activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pem.mensa_app.GlideApp;
import com.pem.mensa_app.R;

import org.joda.time.LocalDateTime;

public class ImageFragment extends Fragment {

    private String imagePath;

    public static ImageFragment newInstance(String imagePath) {
        Bundle args = new Bundle();
        args.putString("imagePath", imagePath);
        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePath = getArguments().getString("imagePath");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_meal_detail, container, false);
        final ImageView imageView = view.findViewById(R.id.imageView_meal_detail);
        //imageView.setImageResource(R.drawable.examplemage);


        StorageReference reference = FirebaseStorage.getInstance().getReference("/images/halbeshendl.png");
        GlideApp.with(view).load(reference).into(imageView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

 }
