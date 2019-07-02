package com.pem.mensa_app.meal_detail_activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.pem.mensa_app.R;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends FragmentStatePagerAdapter {

    private List<String> imagePaths;

    private List<String> defaultPath = new ArrayList<>();


    public ImageAdapter(FragmentManager fm, List<String> iamgePaths) {
        super(fm);
        this.defaultPath.add("images/halbeshendl.png");
        this.imagePaths = imagePaths == null ? defaultPath : iamgePaths;
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
