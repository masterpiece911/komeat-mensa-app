package com.pem.mensa_app.mensa_list_activity;

import android.app.Application;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pem.mensa_app.models.mensa.Occupancy;
import com.pem.mensa_app.models.mensa.RestaurantType;
import com.pem.mensa_app.models.mensa.VisibilityPreference;
import com.pem.mensa_app.utilities.haversine.*;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.pem.mensa_app.models.mensa.Mensa;

import java.util.Arrays;
import java.util.LinkedList;

import javax.annotation.Nullable;

public class MensaListModel extends AndroidViewModel {

    private SortingStrategy sortingStrategy;

    public enum SortingStrategy {
        DISTANCE,
        OCCUPANCY,
        ALPHABETICALLY
    }

    private static final String TAG = "MensaListModel";
    private static final String COLLECTION_IDENTIFIER = "Mensa";
    private static final String PREFERENCES_FAVORITES_IDENTIFIER = "mensa_favorites";
    private static final String PREFERENCES_HIDDEN_IDENTIFIER = "mensa_hidden";

    private final MutableLiveData<LinkedList<Mensa>> mensaData = new MutableLiveData<>();

    private SharedPreferences preferences;
    private LinkedList<String> favouriteItemIds;
    private LinkedList<String> hiddenItemIds;

    private Location location;

    public MensaListModel(Application application) {
        super(application);
        preferences = PreferenceManager.getDefaultSharedPreferences(application);

        try {
            favouriteItemIds = new LinkedList<>(Arrays.asList(preferences.getString(PREFERENCES_FAVORITES_IDENTIFIER, "").split(",")));
        } catch (NullPointerException e) {
            favouriteItemIds = new LinkedList<>();
        }

        try {
            hiddenItemIds = new LinkedList<>(Arrays.asList(preferences.getString(PREFERENCES_HIDDEN_IDENTIFIER, "").split(",")));
        } catch (NullPointerException e) {
            hiddenItemIds = new LinkedList<>();
        }

        sortingStrategy = SortingStrategy.OCCUPANCY;

        loadMensaData();
    }

    LiveData<LinkedList<Mensa>> getMensaData() {
        return mensaData;
    }

    void setLocation(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        this.location = location;
//        if(sortingStrategyWasDistance) {
//            setSortingStrategy(SortingStrategy.DISTANCE);
//        }

        if (getMensaData().getValue() != null) {
            LinkedList<Mensa> fList = new LinkedList<>();
            for(Mensa fItem : getMensaData().getValue()) {
                fItem.setDistance(Haversine.distance(latitude, longitude,
                        fItem.getLatitude(), fItem.getLongitude()));
                fList.add(fItem);
            }
            submitToSort(mensaData.getValue());
        }
    }

    void perishLocations() {
        this.location = null;

    }

    void setSortingStrategy(SortingStrategy sortingStrategy) {
        this.sortingStrategy = sortingStrategy;
        submitToSort(mensaData.getValue());
    }

    SortingStrategy getSortingStrategy() {
        return sortingStrategy;
    }

    void visibilityChanged(Mensa changedItem, VisibilityPreference newVisibility) {
        LinkedList<Mensa> newData = getMensaData().getValue();
        switch (changedItem.getVisibility()){
            case HIDDEN:
                hiddenItemIds.remove(changedItem.getuID());
            case FAVORITE:
                favouriteItemIds.remove(changedItem.getuID());
        }
        int index = newData.indexOf(changedItem);

        changedItem.setVisibility(newVisibility);
        switch (newVisibility) {
            case HIDDEN:
                hiddenItemIds.add(changedItem.getuID());
            case FAVORITE:
                favouriteItemIds.add(changedItem.getuID());
        }


        String hiddenIdsString = TextUtils.join(",", hiddenItemIds.toArray(new String[0]));
        String favoritesIdsString = TextUtils.join(",", favouriteItemIds.toArray(new String[0]));
        preferences.edit()
                .putString(PREFERENCES_HIDDEN_IDENTIFIER, hiddenIdsString)
                .putString(PREFERENCES_FAVORITES_IDENTIFIER, favoritesIdsString)
                .apply();

        newData.remove(index);
        newData.add(changedItem);
        submitToSort(newData);
    }

    private void loadMensaData() {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_IDENTIFIER)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Firebase snapshot error.", e);
                            return;
                        }

                        LinkedList<Mensa> mensaList = new LinkedList<>();
                        Mensa mensaItem;
                        if (snapshots != null) {
                            for (QueryDocumentSnapshot snapshot : snapshots) {
                                if (snapshot.get("name") != null) {
                                    mensaItem = getMensaFromSnapshot(snapshot);
                                    if (mensaItem == null) {continue;}

                                    if(location != null) {
                                        mensaItem.setDistance(
                                                Haversine.distance(
                                                        location.getLatitude(), location.getLongitude(),
                                                        mensaItem.getLatitude(), mensaItem.getLongitude()
                                                ));
                                    }

                                    if(favouriteItemIds.contains(mensaItem.getuID())){
                                        mensaItem.setVisibility(VisibilityPreference.FAVORITE);
                                    } else if (hiddenItemIds.contains(mensaItem.getuID())) {
                                        mensaItem.setVisibility(VisibilityPreference.HIDDEN);
                                    } else {
                                        mensaItem.setVisibility(VisibilityPreference.DEFAULT);
                                    }
                                    mensaList.add(mensaItem);
                                }
                            }
                        }
                        submitToSort(mensaList);
                    }

                    private Mensa getMensaFromSnapshot(QueryDocumentSnapshot snapshot) {
                        Mensa mensaItem;

                        try {
                            mensaItem = new Mensa();
                            mensaItem.setName(snapshot.getString("name"));
                            mensaItem.setAddress(snapshot.getString("address"));
                            mensaItem.setLatitude(snapshot.getGeoPoint("location").getLatitude());
                            mensaItem.setLongitude(snapshot.getGeoPoint("location").getLongitude());
                            mensaItem.setuID(snapshot.getId());
                            mensaItem.setType(RestaurantType.fromString(snapshot.getString("type")));
                            mensaItem.setOccupancy(Occupancy.fromDouble(snapshot.getDouble("occupancy")));
                        } catch (NullPointerException e) {
                            mensaItem = null;
                        }


                        return mensaItem;
                    }
                });
    }

    private void submitToSort(LinkedList<Mensa> list){
        mensaData.postValue(MensaListSorter.sortMensaData(list, getSortingStrategy()));
    }

}
