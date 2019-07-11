package com.pem.mensa_app.meal_detail_activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class ImageAdapter extends FragmentStatePagerAdapter {

    public static final String PLACEHOLDER = "placeholder";

    private ArrayList<String> imagePaths;

    private ArrayList<String> defaultPath = new ArrayList<>();


    public ImageAdapter(FragmentManager fm, ArrayList<String> imagePathList) {
        super(fm);
        this.defaultPath.add(PLACEHOLDER);
        this.imagePaths = imagePathList == null ? defaultPath : imagePathList;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.newInstance(imagePaths.get(position));
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }
}
