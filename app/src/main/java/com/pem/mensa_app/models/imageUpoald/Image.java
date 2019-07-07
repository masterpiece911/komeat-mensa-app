package com.pem.mensa_app.models.imageUpoald;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import org.joda.time.LocalDateTime;

import java.util.Date;
import java.util.List;

/** This class represents the metadata of an image in Firebase */

public class Image {

    /** List of all dishes, which are on this image*/
    private final List<DocumentReference> mMealReferences;

    /** Path to the image in FirebaseStorage */
    private final String mImagePath;

    /** The day, on which the image was taken */
    private final String mTimestamp;

    private final DocumentReference mMealPlanReference;

    public Image(List<DocumentReference> mealReferences, String imagePath, DocumentReference mealPlanUid) {
        this.mMealReferences = mealReferences;
        this.mImagePath = imagePath;
        this.mTimestamp = LocalDateTime.now().toString();
        this.mMealPlanReference = mealPlanUid;
    }

    public List<DocumentReference> getMealReferences() {
        return mMealReferences;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    public DocumentReference getMealPlanReference() {
        return mMealPlanReference;
    }


}
