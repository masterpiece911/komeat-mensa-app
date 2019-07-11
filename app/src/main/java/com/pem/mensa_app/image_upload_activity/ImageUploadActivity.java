package com.pem.mensa_app.image_upload_activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.imageUpoald.Image;
import com.pem.mensa_app.models.imageUpoald.MealSelected;
import com.pem.mensa_app.models.meal.Meal;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ImageUploadActivity extends AppCompatActivity {

    private static final String TAG = ImageUploadActivity.class.getName();

    private static final int REQUEST_TAKE_PHOTO = 1;

    private String currentPhotoPath;

    /** StorageReference for image upload */
    private StorageReference mStorageRef;

    /** DocumentReference for ... */
    private CollectionReference mMealDocumentReference;

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
        mMealDocumentReference = FirebaseFirestore.getInstance().collection(getString(R.string.meal_collection_identifier));

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

                            Log.d(TAG, "loadData complete and successful");
                            FirebaseFirestore instance = FirebaseFirestore.getInstance();
                            final DocumentSnapshot mealplan = task.getResult().getDocuments().get(0);

                            ArrayList<HashMap<String, ArrayList<DocumentReference>>> days = (ArrayList<HashMap<String, ArrayList<DocumentReference>>> ) mealplan.get("days");

                            // TODO day of week
                            for (int i = 0; i < days.size(); i++) {
                                if (i == newDate.getDayOfWeek()-1) {
                                    HashMap<String, ArrayList<DocumentReference>> meals = days.get(i);
                                    Iterator iterator = meals.entrySet().iterator();
                                    ArrayList<DocumentReference> mealDocumentReferenceList = new ArrayList<>();
                                    while(iterator.hasNext()) {
                                        Map.Entry pair = (Map.Entry) iterator.next();
                                        mealDocumentReferenceList = (ArrayList<DocumentReference>) pair.getValue();
                                        iterator.remove();
                                    }
                                    loadMealDescription(mealDocumentReferenceList);
                                }
                            }
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

                            uploadImageMetaData(fileName, uids);

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
    private void uploadImageMetaData(String fileName, List<String> uids) {
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

        final DocumentReference imageReference = FirebaseFirestore.getInstance().collection("Image").document();
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

        // Set data also in meal document
        // Für jeden Essen Array holen, ergänzen und wieder updaten
        for (final DocumentReference docRef : documentReferences) {
            docRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()) {
                                    //Toast.makeText(MealDetailActivity.this, documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                                    Log.d("IamgeUploadActivity", documentSnapshot.getId());

                                    Meal meal = new Meal(
                                            documentSnapshot.getId(),
                                            documentSnapshot.getString("name"),
                                            documentSnapshot.getDouble("price"),
                                            (List<String>) documentSnapshot.get("ingredients"),
                                            (List<String>) documentSnapshot.get("comments"),
                                            (ArrayList<String>) documentSnapshot.get("imagePaths"));

                                    if (meal.getImages() == null) {
                                        ArrayList<String> imagePaths = new ArrayList<>();
                                        imagePaths.add(imageReference.getPath());
                                        meal.setImages(imagePaths);
                                    } else {
                                        meal.getImages().add(imageReference.getPath());
                                    }
                                    updateMealMetaData(meal, docRef);
                                } else {
                                    Toast.makeText(ImageUploadActivity.this, "Document is unknown.", Toast.LENGTH_SHORT).show();
                                    Log.d("ImageUploadActivity", "Document is unknown");
                                }
                            } else {
                                Log.d("ImageUploadActivity", "get failed with: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void updateMealMetaData(Meal meal, DocumentReference mealDocumentReference) {
        Map<String, Object> mealMetadata = new HashMap<>();
        mealMetadata.put("name", meal.getName());
        mealMetadata.put("price", meal.getPrice());
        mealMetadata.put("ingredients", meal.getIngredients());
        mealMetadata.put("comments", meal.getComments());
        mealMetadata.put("imagePaths", meal.getImages());

        mealDocumentReference.set(mealMetadata, SetOptions.merge())
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

        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    private void loadMealDescription(List<DocumentReference> mealDocumentReferenceList) {
        for (DocumentReference mealDocumentReference : mealDocumentReferenceList) {
            FirebaseFirestore.getInstance().collection("Meal").document(mealDocumentReference.getId())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot documentSnapshot = task.getResult();

                        Meal meal = new Meal();
                        meal.setUid(documentSnapshot.getId());
                        MealSelected mealSelected = new MealSelected(documentSnapshot.getId(), documentSnapshot.getString(getString(R.string.meal_field_name)), false);
                        mealSelectedList.add(mealSelected);
                    }
                }
            });
        }
        mRecyclerView.setAdapter(new MealAdapter(mealSelectedList));
    }
}
