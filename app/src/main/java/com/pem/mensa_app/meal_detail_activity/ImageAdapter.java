package com.pem.mensa_app.meal_detail_activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends FragmentStatePagerAdapter {

    private List<String> imagePaths;


    public ImageAdapter(FragmentManager fm, List<String> iamgePaths) {
        super(fm);
        this.imagePaths = imagePaths == null ? new ArrayList<String>() : iamgePaths;
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
