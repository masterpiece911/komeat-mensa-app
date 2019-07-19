package com.pem.mensa_app.image_upload_activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.pem.mensa_app.models.imageUpoald.MealSelected;
import com.pem.mensa_app.models.meal.Meal;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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

    private static final String[] PERMISSIONS_ARRAY = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSION = 200;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int PIC_CROP = 2;

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

    private Button mUploadButtion;



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

        mStorageRef = FirebaseStorage.getInstance().getReference("/images");
        mMealDocumentReference = FirebaseFirestore.getInstance().collection(getString(R.string.meal_collection_identifier));

        // View
        mImageView = findViewById(R.id.imageView_meal_image);
        mUploadButtion = findViewById(R.id.button_upload_image);
        mProgressBar = findViewById(R.id.progressBar_upload_image);

        // Create List
        mRecyclerView = findViewById(R.id.recyclerView_meal_list_image_upload);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        handlePermissionRequest();

        // Listener for Upload Button
        mUploadButtion.setOnClickListener(new View.OnClickListener() {
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
        final LocalDate newDate = date.minusDays(date.getDayOfWeek()).plusDays(mDay + 1);

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
                                if (i == mDay) {
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

            //final DocumentReference imageReference = FirebaseFirestore.getInstance().collection("Image").document();
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
                            finish();

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
                            mUploadButtion.setEnabled(taskSnapshot.getBytesTransferred() == taskSnapshot.getTotalByteCount());
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
    private void uploadImageMetaData(final String fileName, final List<String> uids) {
        uids.addAll(getAllSelectedMeals());
        List<DocumentReference> documentReferences = parseToDocumentReference(uids);

        // Set data in meal document
        for (final DocumentReference docRef : documentReferences) {
            docRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()) {
                                    Log.d(TAG, String.format("Get meal which should be updated: %s", documentSnapshot.getId()));

                                    Meal meal = new Meal(documentSnapshot);

                                    if (meal.getImages() == null) {
                                        ArrayList<String> imagePaths = new ArrayList<>();
                                        imagePaths.add(fileName);
                                        meal.setImages(imagePaths);
                                    } else {
                                        meal.getImages().add(fileName);
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
        mealDocumentReference.set(meal.toMap(), SetOptions.merge())
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
            if (mealSelected.ismIsSelected()) {
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
                Log.d(TAG, ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                mSelectedImageUri = FileProvider.getUriForFile(ImageUploadActivity.this, getPackageName()+ ".mensa_app.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mSelectedImageUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getCanonicalPath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                mImageView.setImageURI(mSelectedImageUri);
                // Crop the captured Image
                performCrop();
            } else if (requestCode == PIC_CROP) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    mSelectedImageUri= result.getUri();
                    mImageView.setImageURI(mSelectedImageUri);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    mImageView.setImageURI(mSelectedImageUri);
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                finish();
            } else if (requestCode == PIC_CROP) {

            }
        }
    }

    private void loadMealDescription(List<DocumentReference> mealDocumentReferenceList) {
        for (DocumentReference mealDocumentReference : mealDocumentReferenceList) {
            String string = mealDocumentReference.getId();
            FirebaseFirestore.getInstance().collection("Meal").document(mealDocumentReference.getId())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot documentSnapshot = task.getResult();

                        addSelectedMeal(documentSnapshot);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ImageUploadActivity.this, e.getMessage(), Toast.LENGTH_LONG);

                }
            });
        }
        mRecyclerView.setAdapter(new MealAdapter(mealSelectedList));
    }

    private void performCrop() {
        Intent intent = CropImage.activity(mSelectedImageUri)
                .setAspectRatio(2, 1)
                .setFixAspectRatio(true)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .getIntent(this);
        startActivityForResult(intent, PIC_CROP);
    }

    private void handlePermissionRequest() {
        // Get permission for location tracking
        if (ContextCompat.checkSelfPermission(ImageUploadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(ImageUploadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                Toast.makeText(ImageUploadActivity.this, "The external storage permission is needed to crop the taken picture",Toast.LENGTH_LONG).show();

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(ImageUploadActivity.this, PERMISSIONS_ARRAY, REQUEST_PERMISSION);
                Toast.makeText(ImageUploadActivity.this, "Request for permission",Toast.LENGTH_SHORT).show();
            }
        } else {
            // Permission has already been granted
            Toast.makeText(ImageUploadActivity.this, "Permission has already been granted.",Toast.LENGTH_SHORT).show();
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Toast.makeText(ImageUploadActivity.this, "Permission was granted, yay!",Toast.LENGTH_SHORT).show();
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(ImageUploadActivity.this, "Do the picture without croping the image.",Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    private void addSelectedMeal(DocumentSnapshot documentSnapshot) {
        MealSelected mealSelected = new MealSelected(documentSnapshot, false);
        mealSelectedList.add(mealSelected);
    }
}
