package com.pem.mensa_app.ui.meal_detail_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.meal.Comment;
import com.pem.mensa_app.ui.image_upload_activity.ImageUploadActivity;
import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.utilities.EdgeDecorator;
import com.pem.mensa_app.utilities.eatapi.MealIngredientParser;
import com.pem.mensa_app.viewmodels.MealDetailViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pem.mensa_app.R.drawable.ic_round_favorite_24px;
import static com.pem.mensa_app.R.drawable.ic_round_favorite_border_24px;

public class MealDetailActivity extends AppCompatActivity {

    private static final String TAG = MealDetailActivity.class.getName();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String mMealUid;
    private String mMealPlanReferencePath;
    private int mDay;

    private ImageAdapter mImageAdapter;
    private ViewPager mViewPager;
    private MaterialButton mButtonTakeImage;
    private MaterialButton mButtonLike;
    private MaterialButton mButtonBack;
    private TextView mTextViewLikeCounter;
    private EditText mEditTextComment;
    private Button mButtonAddComment;
    private TextView mTextViewMealDescription;
    private TextView mTextViewMealIncredients;

    private RecyclerView mCommentRecyclerView;
    private CommentAdapter mCommentAdapter;

    private ArrayList<Comment> mCommentList;
    private ArrayList<String> mCommentTimestampList;

    private MealDetailViewModel mViewModel;

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
        mButtonLike = findViewById(R.id.add_like_customize_button);
        mButtonBack = findViewById(R.id.back_button);
        mTextViewLikeCounter = findViewById(R.id.textView_like_counter);

        mTextViewMealDescription = findViewById(R.id.textView_meal_dishes);
        mTextViewMealIncredients = findViewById(R.id.textView_ingredients);


        mEditTextComment = findViewById(R.id.editText_comment);
        mButtonAddComment = findViewById(R.id.add_comment_customize_button);

        mCommentRecyclerView = findViewById(R.id.recycler_view_comment_list);
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        int padding = getResources().getDimensionPixelSize(R.dimen.padding_comments);
        mCommentRecyclerView.addItemDecoration(new EdgeDecorator(padding, padding));
        mCommentRecyclerView.hasFixedSize();

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

        mButtonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseLikeCounter();
            }
        });

        mButtonBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
            mViewModel = ViewModelProviders.of(this).get(MealDetailViewModel.class);
            mViewModel.setMeal(mMealUid);

            mViewModel.getMealData().observe(this, new Observer<Meal>() {
                @Override
                public void onChanged(Meal meal) {
                    Log.d(TAG, "onChanged: " + meal.getUid());
                    setDataToView(meal);
                    setImageToView(meal);
                }
            });
            mViewModel.getCommentData().observe(this, new Observer<List<Comment>>() {
                @Override
                public void onChanged(List<Comment> comments) {
                    setCommentsToView(comments);
                }
            });
    }

    private void setDataToView(Meal meal) {
        mTextViewMealDescription.setText(meal.getName());

        Pair<String, Integer> ingredientFormat = MealIngredientParser.getPillFromIngredients(meal.getIngredients());

        mTextViewMealIncredients.setText(ingredientFormat.first);
        mTextViewMealIncredients.getBackground().setTint(ingredientFormat.second);



        mTextViewLikeCounter.setText(String.valueOf(meal.getLikeCounter()));
//        mButtonLike.setImageDrawable(meal.getLikeCounter() > 0 ? getDrawable(ic_round_favorite_24px) : getDrawable(ic_round_favorite_border_24px));
        mButtonLike.setIcon(meal.getLikeCounter() > 0 ? getResources().getDrawable(ic_round_favorite_24px, getTheme()) : getResources().getDrawable(ic_round_favorite_border_24px, getTheme()));
//        mButtonLike.set
    }

    private void setCommentsToView(List<Comment> comments) {
        mCommentList = new ArrayList<>(comments);
        if (mCommentList == null) {
            mCommentList = new ArrayList<>();
        }
        mCommentAdapter = new CommentAdapter(mCommentList);
        mCommentRecyclerView.setAdapter(mCommentAdapter);
    }

    private void setImageToView(Meal meal) {
        if (meal.getImages() == null || meal.getImages().equals(Collections.EMPTY_LIST))
            meal.setImages(new ArrayList<String>());
        if(mImageAdapter == null){
            mImageAdapter= new ImageAdapter(getSupportFragmentManager(), meal.getImages());
            mViewPager.setAdapter(mImageAdapter);
        } else {
            mImageAdapter.setImagePaths(meal.getImages());
        }
    }

    private void insertComment(String comment) {
//        mCommentList.add(comment);
//        mCommentAdapter.notifyItemInserted(mCommentList.size()-1);
//        mEditTextComment.getText().clear();
//
//        updateCommentToFirebase();

        mEditTextComment.getText().clear();
        mViewModel.addComment(comment);
    }

    
    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void increaseLikeCounter() {
//        mTextViewLikeCounter.setText()
        mViewModel.incrementLikes();
    }

}
