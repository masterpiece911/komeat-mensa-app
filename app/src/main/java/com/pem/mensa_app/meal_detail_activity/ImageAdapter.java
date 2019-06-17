package com.pem.mensa_app.meal_detail_activity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.pem.mensa_app.R;

public class ImageAdapter extends PagerAdapter {

    private Context context;

    private int[] mImageIds = {R.drawable.examplemage};

    public ImageAdapter(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public int[] getmImageIds() {
        return mImageIds;
    }

    public int getImageAtPosition(int position) {
        return mImageIds[position];
    }

    /**
     * Returns the number of images we have, to create a current number of pages.
     */
    @Override
    public int getCount() {
        return mImageIds.length;
    }

    // Which view belongs to which icon
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(getImageAtPosition(position));
        container.addView(imageView, 0);
        return imageView;
    }

    /**
     * Remove the image, when the items gets destroyed.
     * @param container
     * @param position
     * @param object
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView)object);
    }
}
