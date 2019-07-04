package com.pem.mensa_app.utilities.firebase;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class FirebaseDocumentLiveData extends LiveData<DocumentSnapshot> {

    public static final String TAG = "FirebaseQueryLiveData";
    private DocumentReference documentReference;
    private final MyValueEventListener listener = new MyValueEventListener();
    private ListenerRegistration listenerRegistration;

    public FirebaseDocumentLiveData(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    @Override
    protected void onActive() {
        super.onActive();

        Log.d(TAG, "onActive");
        if (listenerRegistration == null )
            listenerRegistration = documentReference.addSnapshotListener(listener);
    }

    @Override
    protected void onInactive() {
        super.onInactive();

        Log.d(TAG, "onInactive: ");
        if (listenerRegistration != null)
            listenerRegistration.remove();
    }

    private class MyValueEventListener implements EventListener<DocumentSnapshot> {
        @Override
        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
            if (e != null){
                Log.e(TAG, "Can't listen to doc snapshots: " + documentSnapshot + ":::" + e.getMessage());
                return;
            }
            setValue(documentSnapshot);
        }
    }

}
