package com.pem.mensa_app.meal_detail_activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pem.mensa_app.R;
import com.pem.mensa_app.dummy.DummyContent;
import com.pem.mensa_app.image_upload_activity.ImageUploadActivity;
import com.pem.mensa_app.models.meal.Meal;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class MealDetailActivity extends AppCompatActivity implements CommentFragment.OnListFragmentInteractionListener {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Uri mImageUri;
    private String mMealUid;
    private String mMealPlanReferencePath;
    private int mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        // get ID from MensaMealListActivity
        Bundle extras = getIntent().getExtras();

        mMealUid = extras.getString(getString(R.string.intent_meal_uid));
        if (mMealUid == null) {
            Log.d("mealDetailActivity", "There is nothing today!");
        }

        mMealPlanReferencePath = extras.getString("meal_path");

        mDay = extras.getInt("day");

        // get description
        final DocumentReference docRef = db.collection(getString(R.string.meal_collection_identifier)).document(mMealUid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        //Toast.makeText(MealDetailActivity.this, documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                        Log.d("mealDetailActivity", documentSnapshot.getId());

                        Meal meal = new Meal(mMealUid,
                                documentSnapshot.getString("name"),
                                documentSnapshot.getDouble("price"),
                                (List<String>) documentSnapshot.get("ingredients"),
                                (List<String>) documentSnapshot.get("comments"),
                                (List<String>) documentSnapshot.get("imagepaths"));
                        setDataToView(meal);
                        setImageToView(meal.getImages());

                    } else {
                        Toast.makeText(MealDetailActivity.this, "Document is unknown.", Toast.LENGTH_SHORT).show();
                        Log.d("mealDetailActivity", "Document is unknown");
                    }
                } else {
                    Log.d("mealDetailActivity", "get failed with: ", task.getException());
                }
            }
        });

        // RecyclerView for comments
        final RecyclerView recyclerView = findViewById(R.id.comment_fragment);
        recyclerView.setHasFixedSize(true);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // get comments
        DummyContent.DummyItem dummy = new DummyContent.DummyItem( "id", "content", "details");

        List<DummyContent.DummyItem> items = new ArrayList<>();
        items.add(dummy);

        final CommentRecyclerViewAdapter commentRecyclerViewAdapter = new CommentRecyclerViewAdapter(items, this);
        recyclerView.setAdapter(commentRecyclerViewAdapter);

        final Button mButtonChooseImage = findViewById(R.id.button_choose_image);

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    private void setDataToView(Meal meal) {
        TextView textView = findViewById(R.id.textView_meal_dishes);
        textView.setText(meal.getName());

        TextView textViewIngredients = findViewById(R.id.textView_ingredients);
        StringBuilder stringBuilderIngredients = new StringBuilder();
        for (String ingredient : meal.getIngredients()) {
            stringBuilderIngredients.append(ingredient);
            stringBuilderIngredients.append(", ");
        }
        textViewIngredients.setText(stringBuilderIngredients.toString());
    }

    private void setImageToView(List<String> imagePaths) {
        ViewPager viewPager = findViewById(R.id.view_pager);
        ImageAdapter imageAdapter = new ImageAdapter(getSupportFragmentManager(), imagePaths);
        viewPager.setAdapter(imageAdapter);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();

            // Start new Activity und transfer selected image with data.getDataString()
            Intent mealImageIntent = new Intent(MealDetailActivity.this, ImageUploadActivity.class);
            mealImageIntent.putExtra("selected_image", data.getDataString());
            mealImageIntent.putExtra("meal_uid", mMealUid);
            mealImageIntent.putExtra("meal_path", mMealPlanReferencePath);
            mealImageIntent.putExtra("day", mDay);

            startActivity(mealImageIntent);


        }
    }
}
