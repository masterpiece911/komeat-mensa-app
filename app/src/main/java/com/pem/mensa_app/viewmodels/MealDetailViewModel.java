package com.pem.mensa_app.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.pem.mensa_app.models.meal.Comment;
import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.utilities.firebase.FirebaseDocumentLiveData;
import com.pem.mensa_app.utilities.firebase.FirebaseQueryLiveData;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MealDetailViewModel extends ViewModel {

    private static final String TAG = MealDetailViewModel.class.getSimpleName();

    private FirebaseDocumentLiveData mealSnapshot = null;
    private FirebaseQueryLiveData commentsSnapshot = null;
    private MediatorLiveData<Meal> meal = new MediatorLiveData<>();
    private MediatorLiveData<List<Comment>> comments = new MediatorLiveData<>();

    private class MealSnapshotObserver implements Observer<DocumentSnapshot> {
        @Override
        public void onChanged(DocumentSnapshot documentSnapshot) {
            meal.postValue(new Meal(documentSnapshot));
        }
    }

    private class CommentSnapshotObserver implements Observer<QuerySnapshot> {
        @Override
        public void onChanged(QuerySnapshot queryDocumentSnapshots) {
            if (queryDocumentSnapshots.isEmpty()) {
                comments.postValue(new LinkedList<Comment>());
            } else {
                LinkedList<Comment> newList = new LinkedList<>();
                Comment comment;
                for (DocumentSnapshot commentSnapshot : queryDocumentSnapshots) {
                    comment = new Comment();
                    comment.setContent(commentSnapshot.getString("content"));
                    comment.setTimestamp(commentSnapshot.getString("timestamp"));
                    newList.add(comment);
                }
                comments.postValue(newList);
            }
        }
    }

    public MealDetailViewModel() {

    }

    public LiveData<Meal> getMealData() {
        return meal;
    }

    public LiveData<List<Comment>> getCommentData() {
        return comments;
    }

    public void setMeal(String mealUid) {
        meal.removeSource(mealSnapshot);
        comments.removeSource(commentsSnapshot);
        mealSnapshot = new FirebaseDocumentLiveData(
                FirebaseFirestore.getInstance().collection("Meal").document(mealUid)
        );
        commentsSnapshot = new FirebaseQueryLiveData(
                FirebaseFirestore.getInstance().collection("Meal")
                        .document(mealUid).collection("comments")
                        .orderBy("server_timestamp")
        );
        meal.addSource(mealSnapshot, new MealSnapshotObserver());
        comments.addSource(commentsSnapshot, new CommentSnapshotObserver());
    }

    public void addComment(final String commentToAdd) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Meal newMeal = meal.getValue();
        final DocumentReference mRef = db.collection("Meal").document(newMeal.getUid());
        final LocalDateTime now = new LocalDateTime(DateTimeZone.forID("Europe/Berlin"));
        final String commentTimestamp = now.toString("dd.MM.yyyy - kk:mm");
        final HashMap<String, Object> comment = new HashMap<>();
        comment.put("content", commentToAdd);
        comment.put("timestamp", commentTimestamp);
        comment.put("server_timestamp", FieldValue.serverTimestamp());

        DocumentReference commentRef = mRef.collection("comments").document();
        commentRef.set(comment)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if( task.isSuccessful() ) {
                    Log.d(TAG, "comment added");
                }
            }
        });

    }

    public void incrementLikes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Meal").document(meal.getValue().getUid())
                .update("likeCounter", FieldValue.increment(1))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, String.format("Increment Meal %s likes", meal.getValue().getUid()));
                        }
                    }
                });

    }

}
