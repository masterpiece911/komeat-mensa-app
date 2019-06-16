package com.pem.mensa_app;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.Nullable;

public class MensaListModel extends AndroidViewModel {

    private SortingStrategy sortingStrategy;

    public enum SortingStrategy {
        DISTANCE,
        OCCUPANCY
    }

    private static final String TAG = "MensaListModel";
    private static final String COLLECTION_IDENTIFIER = "Mensa";
    private static final String PREFERENCES_FAVORITES_IDENTIFIER = "mensa_favorites";
    private static final String PREFERENCES_HIDDEN_IDENTIFIER = "mensa_hidden";
    private static final String PREFERENCES_PREVIOUSLY_ASKED_LOCATION_PERMISSION = "permission_location_asked";
    private static final String PREFERENCES_WAS_DISTANCE_BEFORE = "permission_location_before";

    private final MutableLiveData<LinkedList<Mensa>> mensaData = new MutableLiveData<>();
    private final LocationSettingManager gpsEnabled;

    private final Comparator<Mensa> distanceComparator = new Comparator<Mensa>() {
        @Override
        public int compare(Mensa o1, Mensa o2) {
            int visibilityResult = Integer.compare(o1.getVisibility().ordinal(), o2.getVisibility().ordinal());
            if (visibilityResult == 0) {
                int distanceResult = Double.compare(o1.getDistance(), o2.getDistance());
                if (distanceResult == 0) {
                    return o1.getName().compareTo(o2.getName());
                } else return distanceResult;
            } else return visibilityResult;
        }
    };

    private final Comparator<Mensa> occupancyComparator = new Comparator<Mensa>() {
        @Override
        public int compare(Mensa o1, Mensa o2) {
            int visibilityResult = Integer.compare(o1.getVisibility().ordinal(), o2.getVisibility().ordinal());
            if (visibilityResult == 0) {
                int occupancyResult = Double.compare(o1.getOccupancy().toInt(), o2.getOccupancy().toInt());
                if (occupancyResult == 0) {
                    return o1.getName().compareTo(o2.getName());
                } else return occupancyResult;
            } else return visibilityResult;
        }
    };

    private SharedPreferences preferences;
    private LinkedList<String> favoritedItemIds;
    private LinkedList<String> hiddenItemIds;

    private Location location;

    public MensaListModel(Application application) {
        super(application);
        preferences = PreferenceManager.getDefaultSharedPreferences(application);

        try {
            favoritedItemIds = new LinkedList<>(Arrays.asList(preferences.getString(PREFERENCES_FAVORITES_IDENTIFIER, "").split(",")));
        } catch (NullPointerException e) {
            favoritedItemIds = new LinkedList<>();
        }

        try {
            hiddenItemIds = new LinkedList<>(Arrays.asList(preferences.getString(PREFERENCES_HIDDEN_IDENTIFIER, "").split(",")));
        } catch (NullPointerException e) {
            hiddenItemIds = new LinkedList<>();
        }

//        hasAskedLocationPermissionBefore = preferences.getBoolean(PREFERENCES_PREVIOUSLY_ASKED_LOCATION_PERMISSION, false);
//        sortingStrategyWasDistance = preferences.getBoolean(PREFERENCES_WAS_DISTANCE_BEFORE, false);

        sortingStrategy = SortingStrategy.OCCUPANCY;

        loadMensaData();
    }

    public LiveData<LinkedList<Mensa>> getMensaData() {
        return mensaData;
    }

    public void setLocation(Location location) {

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
            sortMensaData(fList);
        }
    }

    public void perishLocations() {
        this.location = null;

    }

    public void setSortingStrategy(SortingStrategy sortingStrategy) {
        this.sortingStrategy = sortingStrategy;
        sortMensaData(mensaData.getValue());
    }

    public SortingStrategy getSortingStrategy() {
        return sortingStrategy;
    }

    public void visibilityChanged(Mensa changedItem, VisibilityPreference newVisibility) {
        LinkedList<Mensa> newData = getMensaData().getValue();
        switch (changedItem.getVisibility()){
            case HIDDEN:
                hiddenItemIds.remove(changedItem.getuID());
            case FAVORITE:
                favoritedItemIds.remove(changedItem.getuID());
        }
        int index = newData.indexOf(changedItem);

        changedItem.setVisibility(newVisibility);
        switch (newVisibility) {
            case HIDDEN:
                hiddenItemIds.add(changedItem.getuID());
            case FAVORITE:
                favoritedItemIds.add(changedItem.getuID());
        }


        String hiddenIdsString = TextUtils.join(",", hiddenItemIds.toArray(new String[0]));
        String favoritesIdsString = TextUtils.join(",", favoritedItemIds.toArray(new String[0]));
        preferences.edit()
                .putString(PREFERENCES_HIDDEN_IDENTIFIER, hiddenIdsString)
                .putString(PREFERENCES_FAVORITES_IDENTIFIER, favoritesIdsString)
                .apply();

        newData.remove(index);
        newData.add(changedItem);
        sortMensaData(newData);
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

                                    if(favoritedItemIds.contains(mensaItem.getuID())){
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
                        sortMensaData(mensaList);
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

    private void sortMensaData(LinkedList<Mensa> items) {
        Comparator<Mensa> comparator;
        switch(sortingStrategy){
            case DISTANCE:
                comparator = distanceComparator; break;
            case OCCUPANCY:
                comparator = occupancyComparator; break;
            default:
                comparator = occupancyComparator;
        }
        Collections.sort(items, comparator);
        mensaData.postValue(items);

    }
}
