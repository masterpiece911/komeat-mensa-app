package com.pem.mensa_app.image_upload_activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.imageUpoald.Image;
import com.pem.mensa_app.models.imageUpoald.MealSelected;
import com.pem.mensa_app.models.meal.Meal;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageUploadActivity extends AppCompatActivity {

    private static final String TAG = ImageUploadActivity.class.getName();

    private static final int REQUEST_TAKE_PHOTO = 1;

    private String currentPhotoPath;

    /** StorageReference for image upload */
    private StorageReference mStorageRef;

    /** DocumentReference for ... */
    private DocumentReference mDocRef;

    /** Selected image uri, image for upload */
    private Uri mSelectedImageUri;

    /** Progress bar for image upload */
    private ProgressBar mProgressBar;

    private ImageView mImageView;

    private String mMealUid;

    /** Referenz to meal plan, needed for query */
    private String mMealPlanReferencePath;

    /** Weekday in mealplan, needed for query */
    private int mDay;

    private ArrayList<MealSelected> mealSelectedList = new ArrayList<>();

    private RecyclerView mRecyclerView;
    //private MealAdapter mAdapter;
    //private RecyclerView.LayoutManager mLayoutManager;



    public ImageUploadActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        // Get data from intent
        Bundle extras = getIntent().getExtras();
        //mSelectedImageUri = Uri.parse(extras.getString("selected_image"));
        mMealUid = extras.getString("meal_uid");
        mMealPlanReferencePath = extras.getString("meal_path");
        mDay = extras.getInt("day");

        // Start camera intent
        dispatchTakePictureIntent();

        mStorageRef = FirebaseStorage.getInstance().getReference("/images");
        mDocRef = FirebaseFirestore.getInstance().collection(getString(R.string.meal_collection_identifier)).document("uid");

        // View
        mImageView = findViewById(R.id.imageView_meal_image);
        Button button = findViewById(R.id.button_upload_image);
        mProgressBar = findViewById(R.id.progressBar_upload_image);

        // Create List
        mRecyclerView = findViewById(R.id.recyclerView_meal_list_image_upload);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Listener for Upload Button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        loadData();
    }

    /**
     * Load all dishes of the day from firebase.
     */
    private void loadData() {
        //TODO
        final LocalDate date = new LocalDate(DateTimeZone.forID("Europe/Berlin"));
        final LocalDate newDate = date.minusDays(date.getDayOfWeek()).plusDays(mDay);

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

    /**
     * Upload the image to FirebaseStorage
     */
    private void uploadImage() {
        if (mSelectedImageUri != null) {

            final String fileName = mSelectedImageUri.getLastPathSegment();

            StorageReference fileReference = mStorageRef.child(fileName);

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

                            uploadMetaData(fileName, uids);

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

    /**
     * Metadata for image upload
     * @param fileName Path to image in FirebaseStorage
     * @param uids List with all dishes, which are represented on the image
     */
    private void uploadMetaData(String fileName, List<String> uids) {
        uids.addAll(getAllSelectedMeals());
        List<DocumentReference> documentReferences = parseToDocumentReference(uids);
        String uid = mMealPlanReferencePath.substring(mMealPlanReferencePath.indexOf('/'));
        DocumentReference mealplanReference = FirebaseFirestore.getInstance().collection(uid).document();
        //TODO get the date from query before
        Image image = new Image(documentReferences, fileName, LocalDate.now(), mealplanReference);

        // Prepare Upload
        Map<String, Object> imageMetadata = new HashMap<>();
        imageMetadata.put("dayOfMonth", image.getDayOfMonth());
        imageMetadata.put("month", image.getMonth());
        imageMetadata.put("year", image.getYear());
        imageMetadata.put("image_path", image.getImagePath());
        imageMetadata.put("meal_reference", image.getMealReferences());
        imageMetadata.put("mealplan_reference", image.getMealPlanReference());

        DocumentReference imageReference = FirebaseFirestore.getInstance().collection("Image").document();
        imageReference.set(imageMetadata)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Successfully loaded image metadata to firebase.");
                            //Toast.makeText(ImageUploadActivity.this, "Upload metadata success", Toast.LENGTH_SHORT);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to load image metadata to firebase");
                        //Toast.makeText(ImageUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Parse the data from Firebase into an @{@link MealSelected} Object, to represent all dishes of the day.
     * @param mealPlan
     * @param mealSnapshot
     */
    private void parseMealData(DocumentSnapshot mealPlan, QuerySnapshot mealSnapshot) {
        for(DocumentSnapshot snapshot : mealSnapshot) {
            // Meal erstellen und abspeichern
            Meal meal = new Meal();
            meal.setUid(snapshot.getId());
            MealSelected mealSelected = new MealSelected(snapshot.getId(), snapshot.getString(getString(R.string.meal_field_name)), false);
            mealSelectedList.add(mealSelected);
        }
        mRecyclerView.setAdapter(new MealAdapter(mealSelectedList));
    }

    private List<String> getAllSelectedMeals() {
        List<String> uids = new ArrayList<>();
        for (MealSelected mealSelected : mealSelectedList) {
            if (mealSelected.ismSelected()) {
                uids.add(mealSelected.getUid());
            }
        }
        return uids;
    }

    /**
     * Parses a list with strings, which contains the uids of meals, into a List with {@link DocumentReference}
     * @param uids List with strings, containing all uids of meals
     * @return List with {@link DocumentReference}
     */
    private List<DocumentReference> parseToDocumentReference(final List<String> uids) {
        List<DocumentReference> documentReferences = new ArrayList<>();
        for (String uid : uids) {
            documentReferences.add(FirebaseFirestore.getInstance().collection("Meal").document(uid));
        }
        return documentReferences;
    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.pem.mensa_app",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            mSelectedImageUri = Uri.fromFile(new File(currentPhotoPath));

            // Set image into ImageView
            mImageView.setImageURI(mSelectedImageUri);
        }
    }
}
