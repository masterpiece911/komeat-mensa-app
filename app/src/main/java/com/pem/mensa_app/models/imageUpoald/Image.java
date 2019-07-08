package com.pem.mensa_app.models.imageUpoald;

import com.google.firebase.firestore.DocumentReference;

import org.joda.time.LocalDate;

import java.util.List;

/** This class represents the metadata of an image in Firebase */

public class Image {

    /** List of all dishes, which are on this image*/
    private final List<DocumentReference> mMealReferences;

    /** Path to the image in FirebaseStorage */
    private final String mImagePath;

    /** The day, on which the image was taken */
    private final LocalDate mLocalDate;

    private final DocumentReference mMealPlanReference;

    public Image(List<DocumentReference> mealReferences, String imagePath, LocalDate localDate, DocumentReference mealPlanUid) {
        this.mMealReferences = mealReferences;
        this.mImagePath = imagePath;
        this.mLocalDate = localDate;
        this.mMealPlanReference = mealPlanUid;
    }

    public List<DocumentReference> getMealReferences() {
        return mMealReferences;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public LocalDate getLocalDate() {
        return mLocalDate;
    }

    public int getDayOfMonth() { return mLocalDate.getDayOfMonth(); }

    public int getMonth() { return mLocalDate.getMonthOfYear(); }

    public int getYear() { return mLocalDate.getYear(); }

    public DocumentReference getMealPlanReference() {
        return mMealPlanReference;
    }


}
