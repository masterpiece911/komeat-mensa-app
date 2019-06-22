package com.pem.mensa_app.meal_detail_activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        DocumentReference docRef = db.collection("Meal").document("p7SSMzqa4qD61BY1kroM");

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Toast.makeText(MealDetailActivity.this, documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                        Log.d("mealDetailActivity", documentSnapshot.getId());
                    } else {
                        Toast.makeText(MealDetailActivity.this, "Document is unknown.", Toast.LENGTH_SHORT).show();
                        Log.d("mealDetailActivity", "Document is unknown");
                    }
                } else {
                    Log.d("mealDetailActivity", "get failed with: ", task.getException());
                }
            }
        });

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
