package com.pem.mensa_app.image_upload_activity;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pem.mensa_app.MealListModel;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.imageUpoald.Image;
import com.pem.mensa_app.models.imageUpoald.MealSelected;
import com.pem.mensa_app.models.meal.Meal;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ImageUploadActivity extends AppCompatActivity {

    private static final String TAG = ImageUploadActivity.class.getName();

    private StorageReference mStorageRef;
    private DocumentReference mDocRef;
    private Uri mSelectedImageUri;
    private ProgressBar mProgressBar;
    private String mMealUid;
    private String mMealPlanReferencePath;
    private int mDay;

    private ArrayList<MealSelected> mealSelectedList = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private MealAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;



    public ImageUploadActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        // Get data from intent
        Bundle extras = getIntent().getExtras();
        mSelectedImageUri = Uri.parse(extras.getString("selected_image"));
        mMealUid = extras.getString("meal_uid");
        mMealPlanReferencePath = extras.getString("meal_path");
        mDay = extras.getInt("day");

        mStorageRef = FirebaseStorage.getInstance().getReference("/images");
        mDocRef = FirebaseFirestore.getInstance().collection(getString(R.string.meal_collection_identifier)).document("uid");


        // View
        ImageView imageView = findViewById(R.id.imageView_meal_image);
        Button button = findViewById(R.id.button_upload_image);
        mProgressBar = findViewById(R.id.progressBar_upload_image);

        // Set image into ImageView
        imageView.setImageURI(mSelectedImageUri);

        mRecyclerView = findViewById(R.id.recyclerView_meal_list_image_upload);
        mLayoutManager = new LinearLayoutManager(this);
//        mAdapter = new MealAdapter(mealSelectedList);
        mRecyclerView.setLayoutManager(mLayoutManager);
//        mRecyclerView.setAdapter(mAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        loadData();
    }

    private void loadData() {
        // TODO
        final LocalDate date = new LocalDate(DateTimeZone.forID("Europe/Berlin"));
        LocalDate newDate = date.minusDays(date.getDayOfWeek()).plusDays(mDay);

        Log.d(TAG, String.format("old date %s, new date %s", date.toString(), newDate.toString()));

        FirebaseFirestore.getInstance().collection(mMealPlanReferencePath + "/items")
                .whereEqualTo(getApplication().getString(R.string.mealplan_field_year), newDate.year().get())
                .whereEqualTo(getApplication().getString(R.string.mealplan_field_week), newDate.weekOfWeekyear().get())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            //todo surface items
                            Log.d(TAG, "loadDataFromFirebase complete and successful");
                            FirebaseFirestore instance = FirebaseFirestore.getInstance();
                            final DocumentSnapshot mealplan = task.getResult().getDocuments().get(0);
                            instance.collection(getString(R.string.meal_collection_identifier))
                                    .whereEqualTo("mealplan", instance.collection(mMealPlanReferencePath + "/items").document(mealplan.getId()))
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful() && !task.getResult().isEmpty()) {
                                        Log.d(TAG, "load meal data complete and successful.");
                                        Log.d(TAG, String.format("result is %d large", task.getResult().size()));
                                        parseMealData(mealplan, task.getResult());
                                    }
                                }
                            });


                        } else if (task.getResult().isEmpty()) {
                            Log.d(TAG, "No mealplan for selected week found. Generating.");
                            Toast.makeText(ImageUploadActivity.this, "No data avaiable!", Toast.LENGTH_LONG);
                        }
                    }
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImage() {
        if (mSelectedImageUri != null) {

            final String filename = System.currentTimeMillis()
                    + "." + getFileExtension(mSelectedImageUri);

            StorageReference fileReference = mStorageRef.child(filename);

            fileReference.putFile(mSelectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // the handler delay the rest of the progressbar for 5 seconds
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 5000);
                            Toast.makeText(ImageUploadActivity.this, "Upload success", Toast.LENGTH_LONG).show();

                            // save the metadata from the image
                            // List of selected meals
                            List<String> uids = new ArrayList<>();
                            uids.add(mMealUid);

                            Image image = new Image(uids, filename, LocalDateTime.now(), "");

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ImageUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });


        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void parseMealData(DocumentSnapshot mealPlan, QuerySnapshot mealSnapshot) {
        for(DocumentSnapshot snapshot : mealSnapshot) {
            MealSelected mealSelected = new MealSelected(snapshot.getString(getString(R.string.meal_field_name)), false);
            mealSelectedList.add(mealSelected);
        }
        mRecyclerView.setAdapter(new MealAdapter(mealSelectedList));
    }
}
