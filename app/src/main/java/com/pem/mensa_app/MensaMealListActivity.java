package com.pem.mensa_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pem.mensa_app.meal_detail_activity.MealDetailActivity;
import com.pem.mensa_app.models.meal.Meal;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class MensaMealListActivity extends AppCompatActivity implements MealListAdapter.MealClickEventListener {

    private static final String TAG = MensaMealListActivity.class.getSimpleName();

    private MealListModel viewModel;
    private RecyclerView mRecyclerView;
    private MealListAdapter mMealListAdapter;
    private RadioGroup mRadioGroup;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensa_meal_list);

        Bundle extras = getIntent().getExtras();

        viewModel = ViewModelProviders.of(this).get(MealListModel.class);
        viewModel.setData(extras);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mMealListAdapter = new MealListAdapter(this);
        mRecyclerView.setAdapter(mMealListAdapter);

        viewModel.getMealData().observe(this, new Observer<LinkedList<Meal>>() {
            @Override
            public void onChanged(LinkedList<Meal> meals) {
                mMealListAdapter.submitList(new LinkedList<>(meals));
                mMealListAdapter.notifyDataSetChanged();
            }
        });

        viewModel.informationSet();

        mRadioGroup = findViewById(R.id.meal_list_control);
        int rBtoCheck = 0;
        switch(viewModel.getSelectedWeekday()){
            case 1: rBtoCheck = R.id.radioButton; break;
            case 2: rBtoCheck = R.id.radioButton2; break;
            case 3: rBtoCheck = R.id.radioButton3; break;
            case 4: rBtoCheck = R.id.radioButton4; break;
            case 5: rBtoCheck = R.id.radioButton5; break;
        }
        mRadioGroup.check(rBtoCheck);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton clickedButton = findViewById(i);
                int weekday = 0;
                switch(clickedButton.getText().toString()) {
                    case "Mo": weekday = 1; break;
                    case "Tue": weekday = 2; break;
                    case "Wed": weekday = 3; break;
                    case "Thu": weekday = 4; break;
                    case "Fr": weekday = 5; break;
                }
                viewModel.setSelectedWeekday(weekday);
            }
        });

    }

    @Override
    public void onMealClick(int position) {

        Meal clickedMeal = viewModel.getMealData().getValue().get(position);

        Intent mealDetailIntent = new Intent(MensaMealListActivity.this, MealDetailActivity.class);
        mealDetailIntent.putExtra(getString(R.string.intent_meal_uid), clickedMeal.getUid());
        startActivity(mealDetailIntent);

    }

    String currentPhotoPath;

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

    static final int REQUEST_TAKE_PHOTO = 1;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {


            // File or Blob
             Uri file = Uri.fromFile(new File(currentPhotoPath));

// Create the file metadata
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();
            StorageReference storageRef= FirebaseStorage.getInstance().getReference();
// Upload file and metadata to the path 'images/mountains.jpg'
            UploadTask uploadTask = storageRef.child("images/"+file.getLastPathSegment()).putFile(file, metadata);

// Listen for state changes, errors, and completion of the upload.
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("Upload is paused");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Handle successful uploads on complete
                    // ...
                }
            });

        }
    }



    @Override
    public void onImageButtonClick(int position) {

        Log.d(TAG, "onImageButtonClick: " + position);

        dispatchTakePictureIntent();
    }
}
