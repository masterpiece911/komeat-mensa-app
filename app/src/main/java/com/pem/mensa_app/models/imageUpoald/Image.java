package com.pem.mensa_app.models.imageUpoald;

import org.joda.time.LocalDateTime;

import java.util.List;

/** This class represents the metadata of an image in Firebase */

public class Image {

    /** List of all dishes, which are on this image*/
    List<String> mMealUids;

    /** Path to the image in FirebaseStorage */
    String mImagePath;

    /** The day, on which the image was taken */
    LocalDateTime mDate;

    String mMealPlanUid;

    public Image(List<String> uids, String imagePath, LocalDateTime localDateTime, String mealPlanUid) {
        this.mMealUids = uids;
        this.mImagePath = imagePath;
        this.mDate = localDateTime;
        this.mMealPlanUid = mealPlanUid;
    }

    public List<String> getmMealUids() {
        return mMealUids;
    }

    public void setmMealUids(List<String> mMealUids) {
        this.mMealUids = mMealUids;
    }

    public String getmImagePath() {
        return mImagePath;
    }

    public void setmImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public LocalDateTime getmDate() {
        return mDate;
    }

    public void setmDate(LocalDateTime mDate) {
        this.mDate = mDate;
    }

    public String getmMealPlanUid() {
        return mMealPlanUid;
    }

    public void setmMealPlanUid(String mMealPlanUid) {
        this.mMealPlanUid = mMealPlanUid;
    }
}
