package com.pem.mensa_app.ui.home_activity;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.AndroidViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.models.mensa.RestaurantType;
import com.pem.mensa_app.utilities.firebase.FirebaseQueryLiveData;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = HomeViewModel.class.getSimpleName();

    private static final String PREFERENCES_FAVORITES_IDENTIFIER = "mensa_favorites_new";
    private static final String[] DEFAULT_FAVORITES = {
            "1zWkH9T1gKOE9wEWFpng", //mensa akademie weihenstephan
            "45KkDBxESsqXFy6pVBoj", //mensa garching
            "INMTRb5aTOiIbiRvgSt9", //stucafe adalbertstr
            "N9cedTGoIYpoBR2PWxRm", //mensa leopoldstr
            "riHTJpl9MRslCxxvd4Kt", //mensa arcisstr
            "TO0wkynjhzHgruF6Xt9s", //mensa martinsried
    };
    private final SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    public HomeViewModel(Application app) {
        super(app);
        preferences = PreferenceManager.getDefaultSharedPreferences(app);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(PREFERENCES_FAVORITES_IDENTIFIER)) {
                    favoriteMensaIDs.postValue(getFavoriteMensaIDs());
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);

        favoriteMensaIDs.postValue(getFavoriteMensaIDs());

        FirebaseQueryLiveData mensaSnapshot = new FirebaseQueryLiveData(mensaListQuery);
        mensaList.addSource(mensaSnapshot, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(QuerySnapshot snapshot) {
                Executors.defaultThreadFactory().newThread(new MensaListDeserialiser(snapshot)).run();
            }
        });

        MensaSortData mensaSortTrigger = new MensaSortData(mensaList, favoriteMensaIDs);
        favoriteMensaList.addSource(mensaSortTrigger, new Observer<Pair<List<Mensa>, List<String>>>() {
            @Override
            public void onChanged(Pair<List<Mensa>, List<String>> sorter) {
                if (sorter.first == null || sorter.second == null) {
                    favoriteMensaList.postValue(new LinkedList<Mensa>());
                    return;
                }
                LinkedList<Mensa> mensas = new LinkedList<>(sorter.first);
                LinkedList<String> ids = new LinkedList<>(sorter.second);
                LinkedList<Mensa> sortedMensas = new LinkedList<>();

                for (Mensa m : mensas) {
                    if (ids.contains(m.getuID())){
                        sortedMensas.add(m);
                        ids.remove(m.getuID());
                    }
                }

                favoriteMensaList.postValue(sortedMensas);

            }
        });

    }
    private final Query mensaListQuery = FirebaseFirestore.getInstance().collection(getApplication().getString(R.string.mensa_collection_identifier));

    private final MutableLiveData<List<String>> favoriteMensaIDs = new MutableLiveData<>();
    private final MediatorLiveData<List<Mensa>> mensaList = new MediatorLiveData<>();
    private final MediatorLiveData<List<Mensa>> favoriteMensaList = new MediatorLiveData<>();

    public LiveData<List<Mensa>> getMensaList() {
        return mensaList;
    }

    public LiveData<List<Mensa>> getFavoriteMensaList() {
        return favoriteMensaList;
    }

    public void flipFavoriteMensa(Mensa toFlip) {
        HashSet<String> defaults = new HashSet<>();
        defaults.addAll(Arrays.asList(DEFAULT_FAVORITES));
        HashSet<String> newFavorites = new HashSet<>(preferences.getStringSet(PREFERENCES_FAVORITES_IDENTIFIER, defaults));
        if(newFavorites.contains(toFlip.getuID())) {
            newFavorites.remove(toFlip.getuID());
            if (newFavorites.isEmpty()) {
                preferences.edit().remove(PREFERENCES_FAVORITES_IDENTIFIER).apply();
            }
            preferences.edit().putStringSet(PREFERENCES_FAVORITES_IDENTIFIER, newFavorites).apply();
        } else {
            newFavorites.add(toFlip.getuID());
            preferences.edit().putStringSet(PREFERENCES_FAVORITES_IDENTIFIER, newFavorites).apply();
        }
    }

    private List<String> getFavoriteMensaIDs() {
        HashSet<String> defaults = new HashSet<>(Arrays.asList(DEFAULT_FAVORITES));
//        return Arrays.asList(preferences.getStringSet(PREFERENCES_FAVORITES_IDENTIFIER, defaults).toArray(new String[0]));
        Set<String> favorites = preferences.getStringSet(PREFERENCES_FAVORITES_IDENTIFIER, defaults);

        for(String fav : favorites.toArray(new String[0])) {
            Log.d(TAG, fav);
        }

        return Arrays.asList(favorites.toArray(new String[0]));

    }

    class MensaListDeserialiser implements Runnable {

        QuerySnapshot snapshot;

        MensaListDeserialiser(QuerySnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Mensa mensa ;
            LinkedList<Mensa> mMensaList = new LinkedList<>();
            for (DocumentSnapshot mensaSnapshot : snapshot.getDocuments()){
                mensa = new Mensa();
                mensa.setName(mensaSnapshot.getString(getApplication().getString(R.string.mensa_field_name)));
                mensa.setAddress(mensaSnapshot.getString(getApplication().getString(R.string.mensa_field_address)));
                mensa.setuID(mensaSnapshot.getId());
                GeoPoint geo = mensaSnapshot.getGeoPoint(getApplication().getString(R.string.mensa_field_location));
                mensa.setLatitude(geo.getLatitude());
                mensa.setLongitude(geo.getLongitude());
                mensa.setType(RestaurantType.fromString(mensaSnapshot.getString(getApplication().getString(R.string.mensa_field_type))));
                mensa.setUrl(mensaSnapshot.getString(getApplication().getString(R.string.mensa_field_url)));
                mMensaList.add(mensa);
            }
            mensaList.postValue(mMensaList);
        }
    }

    class MensaSortData extends MediatorLiveData<Pair<List<Mensa>, List<String>>> {
        public MensaSortData (final LiveData<List<Mensa>> mensas, final LiveData<List<String>> favoriteIDs) {
            addSource(mensas, new Observer<List<Mensa>>() {
                @Override
                public void onChanged(List<Mensa> first) {
                    setValue(Pair.create(first, favoriteIDs.getValue()));
                }
            });
            addSource(favoriteIDs, new Observer<List<String>>() {
                @Override
                public void onChanged(List<String> second) {
                    setValue(Pair.create(mensas.getValue(), second));
                }
            });
        }
    }

}