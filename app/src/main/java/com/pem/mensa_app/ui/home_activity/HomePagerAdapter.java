package com.pem.mensa_app.ui.home_activity;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.pem.mensa_app.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.home_tab_1, R.string.home_tab_2, R.string.home_tab_3};
    private static final int NUM_PAGES = 3;
    private final Context mContext;

    public HomePagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return HomeFeedFragment.newInstance();
            case 1: return MapFragment.newInstance();
            case 2: return MensaListFragment.newInstance(false, true);
            default: return MensaListFragment.newInstance(false, false);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {

        return NUM_PAGES;
    }
}