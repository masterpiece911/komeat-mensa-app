package com.pem.mensa_app.image_upload_activity;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.imageUpoald.Image;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class ImageUploadActivity extends AppCompatActivity {

    private StorageReference mStorageRef;
    private DocumentReference mDocRef;
    private Uri mSelectedImageUri;
    private ProgressBar mProgressBar;
    private String mMealUid;

    public ImageUploadActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        ImageView imageView = findViewById(R.id.imageView_meal_image);
        Button button = findViewById(R.id.button_upload_image);
        mProgressBar = findViewById(R.id.progressBar_upload_image);

        // Get data from intent
        Bundle extras = getIntent().getExtras();
        mSelectedImageUri = Uri.parse(extras.getString("selected_image"));
        mMealUid = extras.getString("meal_uid");

        mStorageRef = FirebaseStorage.getInstance().getReference("/images");
        mDocRef = FirebaseFirestore.getInstance().collection(getString(R.string.meal_collection_identifier)).document("uid");

        // Set image into ImageView
        imageView.setImageURI(mSelectedImageUri);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
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

                            Image image = new Image(uids, filename, LocalDateTime.now());

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
}
