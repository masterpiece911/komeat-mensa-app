package com.pem.mensa_app.meal_detail_activity;

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
import com.google.firebase.firestore.Transaction;
import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.utilities.firebase.FirebaseDocumentLiveData;

public class MealDetailViewModel extends ViewModel {

    private static final String TAG = MealDetailViewModel.class.getSimpleName();

    private FirebaseDocumentLiveData mealSnapshot = null;
    private MediatorLiveData<Meal> meal = new MediatorLiveData<>();

    private class MealSnapshotObserver implements Observer<DocumentSnapshot> {
        @Override
        public void onChanged(DocumentSnapshot documentSnapshot) {
            meal.postValue(new Meal(documentSnapshot));
        }
    }

    public MealDetailViewModel() {

    }

    public LiveData<Meal> getMealData() {
        return meal;
    }

    public void setMeal(String mealUid) {
        meal.removeSource(mealSnapshot);
        mealSnapshot = new FirebaseDocumentLiveData(
                FirebaseFirestore.getInstance().collection("Meal").document(mealUid)
        );
        meal.addSource(mealSnapshot, new MealSnapshotObserver());
    }

    public void addComment(final String commentToAdd) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final Meal newMeal = meal.getValue();
        final DocumentReference mealRef = db.collection("Meal").document(newMeal.getUid());

        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                transaction.get(mealRef);
                transaction.update(mealRef, "comments", newMeal.getComments().add(commentToAdd));

                return null;
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
