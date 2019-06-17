package com.pem.mensa_app.meal_detail_activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.pem.mensa_app.R;
import com.pem.mensa_app.dummy.DummyContent;

import java.util.ArrayList;
import java.util.List;

public class MealDetailActivity extends AppCompatActivity implements CommentFragment.OnListFragmentInteractionListener {

    private RecyclerView recyclerView;
    private CommentFragment commentFragment;
    private CommentFragment.OnListFragmentInteractionListener onListFragmentInteractionListener;
    private CommentRecyclerViewAdapter commentRecyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        ViewPager viewPager = findViewById(R.id.view_pager);
        ImageAdapter imageAdapter = new ImageAdapter(this); // pass the context to the adapter
        viewPager.setAdapter(imageAdapter);

        recyclerView = findViewById(R.id.comment_fragment);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DummyContent.DummyItem dummy = new DummyContent.DummyItem( "id", "content", "details");

        List<DummyContent.DummyItem> items = new ArrayList<>();
        items.add(dummy);

        commentRecyclerViewAdapter = new CommentRecyclerViewAdapter(items, onListFragmentInteractionListener);
        recyclerView.setAdapter(commentRecyclerViewAdapter);


    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
