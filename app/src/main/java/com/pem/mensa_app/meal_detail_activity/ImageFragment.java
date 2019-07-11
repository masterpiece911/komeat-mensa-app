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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pem.mensa_app.GlideApp;
import com.pem.mensa_app.R;

import org.joda.time.LocalDateTime;

public class ImageFragment extends Fragment {

    private String imagePath;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        if (imagePath.equals(ImageAdapter.PLACEHOLDER)) {
            imageView.setImageResource(R.drawable.placeholder);
        } else {
            // TODO at this point the image reference is needed to get the image from firebase
            db.document(imagePath)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        String image_path = (String) documentSnapshot.get("image_path");

                        StorageReference reference = FirebaseStorage.getInstance().getReference("images/" + image_path);
                        GlideApp.with(view).load(reference).into(imageView);
                    }
                }
            });
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

 }
