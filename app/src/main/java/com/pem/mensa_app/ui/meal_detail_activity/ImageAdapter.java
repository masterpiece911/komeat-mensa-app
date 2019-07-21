package com.pem.mensa_app.ui.meal_detail_activity;

import androidx.annotation.NonNull;
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
        this.imagePaths = imagePathList == null || (imagePathList != null && imagePathList.isEmpty()) ? defaultPath : imagePathList;
    }

    public void setImagePaths(ArrayList<String> imagePaths) {
        this.imagePaths = imagePaths;
        this.notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.newInstance(imagePaths.get(position));
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        ImageFragment frag = (ImageFragment) object;
        String path = frag.getImagePath();
        int position = imagePaths.indexOf(path);

        if (position >= 0) {
            return position;
        } else return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }
}
