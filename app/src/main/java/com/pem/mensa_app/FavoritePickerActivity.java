package com.pem.mensa_app;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;

import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.ui.home_activity.FavoritePickerPagerAdapter;
import com.pem.mensa_app.ui.home_activity.OnMensaItemSelectedListener;

public class FavoritePickerActivity extends FragmentActivity implements OnMensaItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_picker);

        FavoritePickerPagerAdapter favoritePickerPagerAdapter = new FavoritePickerPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.favorite_picker_viewpager);
        viewPager.setAdapter(favoritePickerPagerAdapter);

        Toolbar toolbar = findViewById(R.id.favorite_picker_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    @Override
    public void onMensaSelected(Mensa mensa) {
        return;
    }
}
