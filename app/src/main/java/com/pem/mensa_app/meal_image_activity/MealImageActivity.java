package com.pem.mensa_app.meal_image_activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.pem.mensa_app.R;

public class MealImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_image);

        ImageView mImageView = findViewById(R.id.imageView_meal_image);

        Bundle extras = getIntent().getExtras();
        Uri mSelectedImageUri = Uri.parse(extras.getString("selected_image"));

        mImageView.setImageURI(mSelectedImageUri);
    }
}
