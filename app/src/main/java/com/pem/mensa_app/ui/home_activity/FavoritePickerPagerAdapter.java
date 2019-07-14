package com.pem.mensa_app.ui.home_activity;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FavoritePickerPagerAdapter extends FragmentPagerAdapter {

    private static final int NUM_PAGES = 1;
    final Context mContext;

    public FavoritePickerPagerAdapter(Context context, FragmentManager fm){
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return MensaListFragment.newInstance(true, false);
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
