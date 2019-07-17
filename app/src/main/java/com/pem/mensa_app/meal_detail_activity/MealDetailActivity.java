package com.pem.mensa_app.meal_detail_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.pem.mensa_app.image_upload_activity.ImageUploadActivity;
import com.pem.mensa_app.models.meal.Meal;

import java.util.ArrayList;
import java.util.List;

public class MealDetailActivity extends AppCompatActivity {

    private static final String TAG = MealDetailActivity.class.getName();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String mMealUid;
    private String mMealPlanReferencePath;
    private int mDay;

    private Meal mMeal;

    private ImageAdapter mImageAdapter;
    private ViewPager mViewPager;
    private Button mButtonTakeImage;
    private EditText mEditTextComment;
    private Button mButtonAddComment;
    private TextView mTextViewMealDescription;
    private TextView mTextViewMealIncredients;

    private RecyclerView mCommentRecyclerView;
    private CommentAdapter mCommentAdapter;

    private ArrayList<String> mCommentList;

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

        mViewPager = findViewById(R.id.view_pager);
        mButtonTakeImage = findViewById(R.id.add_picture_customize_button);

        mTextViewMealDescription = findViewById(R.id.textView_meal_dishes);
        mTextViewMealIncredients = findViewById(R.id.textView_ingredients);


        mEditTextComment = findViewById(R.id.editText_comment);
        mButtonAddComment = findViewById(R.id.add_comment_customize_button);

        mCommentRecyclerView = findViewById(R.id.recycler_view_comment_list);
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentRecyclerView.hasFixedSize();

        getMealDataFromFirebase();


        mButtonTakeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageUploadActivityIntent = new Intent(MealDetailActivity.this, ImageUploadActivity.class);
                imageUploadActivityIntent.putExtra("meal_uid", mMealUid);
                imageUploadActivityIntent.putExtra("meal_path", mMealPlanReferencePath);
                imageUploadActivityIntent.putExtra("day", mDay);
                startActivity(imageUploadActivityIntent);

                //TODO bei der Rückkehr ImageListe neu laden
            }
        });

        mEditTextComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mButtonAddComment.setEnabled(!mEditTextComment.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mButtonAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String comment =  mEditTextComment.getText().toString();
               insertComment(comment);
               closeKeyboard();
            }
        });
    }

    private void getMealDataFromFirebase() {
        final DocumentReference docRef = db.collection(getString(R.string.meal_collection_identifier)).document(mMealUid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        //Toast.makeText(MealDetailActivity.this, documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                        Log.d("mealDetailActivity", documentSnapshot.getId());

                        mMeal = new Meal(documentSnapshot);
                        setDataToView();
                        setImageToView();

                    } else {
                        Toast.makeText(MealDetailActivity.this, "Document is unknown.", Toast.LENGTH_SHORT).show();
                        Log.d("mealDetailActivity", "Document is unknown");
                    }
                } else {
                    Log.d("mealDetailActivity", "get failed with: ", task.getException());
                }
            }
        });
    }

    private void insertComment(String comment) {
        mCommentList.add(comment);
        mCommentAdapter.notifyItemInserted(mCommentList.size()-1);
        mEditTextComment.getText().clear();

        updateCommentToFirebase();
    }

    private void setDataToView() {
        mTextViewMealDescription.setText(mMeal.getName());

        StringBuilder stringBuilderIngredients = new StringBuilder();
        for (String ingredient : mMeal.getIngredients()) {
            stringBuilderIngredients.append(ingredient);
            stringBuilderIngredients.append(", ");
        }
        mTextViewMealIncredients.setText(stringBuilderIngredients.toString());

        mCommentList = mMeal.getComments();
        if (mCommentList == null) {
            mCommentList = new ArrayList<>();
        }
        mCommentAdapter = new CommentAdapter(mCommentList);
        mCommentRecyclerView.setAdapter(mCommentAdapter);
    }

    private void setImageToView() {
        if (mMeal.getImages() == null)
            mMeal.setImages(new ArrayList<String>());
        mImageAdapter= new ImageAdapter(getSupportFragmentManager(), mMeal.getImages());
        mViewPager.setAdapter(mImageAdapter);
    }

    private void updateCommentToFirebase() {
        final DocumentReference docRef = db.collection(getString(R.string.meal_collection_identifier)).document(mMealUid);
        docRef.update("comments", mCommentList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Successfully loaded comments to firebase.");
                    Toast.makeText(MealDetailActivity.this, "Upload comments success", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
