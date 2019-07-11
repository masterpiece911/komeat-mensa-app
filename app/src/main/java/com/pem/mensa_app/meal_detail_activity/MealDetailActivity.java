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
import com.google.firebase.firestore.QuerySnapshot;
import com.pem.mensa_app.MensaMealListActivity;
import com.pem.mensa_app.R;
import com.pem.mensa_app.dummy.DummyContent;
import com.pem.mensa_app.image_upload_activity.ImageUploadActivity;
import com.pem.mensa_app.models.meal.Meal;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class MealDetailActivity extends AppCompatActivity implements CommentFragment.OnListFragmentInteractionListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Uri mImageUri;
    private String mMealUid;
    private String mMealPlanReferencePath;
    private int mDay;

    private ImageAdapter mImageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

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

                        Meal meal = new Meal(documentSnapshot.getId(),
                                documentSnapshot.getString("name"),
                                documentSnapshot.getDouble("price"),
                                (List<String>) documentSnapshot.get("ingredients"),
                                (List<String>) documentSnapshot.get("comments"),
                                (ArrayList<String>) documentSnapshot.get("imagePaths"));
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

        mViewPager = findViewById(R.id.view_pager);

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
                Intent imageUploadActivityIntent = new Intent(MealDetailActivity.this, ImageUploadActivity.class);
                imageUploadActivityIntent.putExtra("meal_uid", mMealUid);
                imageUploadActivityIntent.putExtra("meal_path", mMealPlanReferencePath);
                imageUploadActivityIntent.putExtra("day", mDay);
                startActivity(imageUploadActivityIntent);
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

    private void setImageToView(ArrayList<String> imagePaths) {
        mImageAdapter= new ImageAdapter(getSupportFragmentManager(), imagePaths);
        mViewPager.setAdapter(mImageAdapter);
    }
}
