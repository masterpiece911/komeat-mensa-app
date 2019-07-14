package com.pem.mensa_app.ui.home_activity;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.AndroidViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.pem.mensa_app.R;
import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.models.mensa.MensaDay;
import com.pem.mensa_app.models.mensa.RestaurantType;
import com.pem.mensa_app.utilities.eatapi.ApiDataLoader;
import com.pem.mensa_app.utilities.firebase.FirebaseQueryLiveData;

import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = HomeViewModel.class.getSimpleName();

    private final Executor executor = Executors.newSingleThreadExecutor();

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

    private final Query mensaListQuery = FirebaseFirestore.getInstance().collection(getApplication().getString(R.string.mensa_collection_identifier));

    private final MutableLiveData<List<String>> favoriteMensaIDs = new MutableLiveData<>();
    private final MediatorLiveData<List<Mensa>> mensaList = new MediatorLiveData<>();
    private final MediatorLiveData<List<Mensa>> favoriteMensaList = new MediatorLiveData<>();
    private final MediatorLiveData<List<Pair<Mensa, QuerySnapshot>>> mensaMealsSnapshots = new MediatorLiveData<>();
    private final MediatorLiveData<List<MensaDay>> mensaMealDetails = new MediatorLiveData<>();

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

        mensaList.observeForever(new Observer<List<Mensa>>() {
            @Override
            public void onChanged(List<Mensa> mensas) {
                LinkedList<Mensa> emptyMensas = new LinkedList<>();
                for (Mensa m : mensas) {
                    if (m.getUrl() != null && m.getMealPlanReference() == null) {
                        emptyMensas.add(m);
                    }
                }
                Executors.defaultThreadFactory().newThread(new MensaMealPlanCreator(emptyMensas)).run();
                mensaList.removeObserver(this);
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

        mensaMealsSnapshots.addSource(favoriteMensaList, new MensaFavoritesObserver());

        mensaMealDetails.addSource(mensaMealsSnapshots, new Observer<List<Pair<Mensa, QuerySnapshot>>>() {
            @Override
            public void onChanged(List<Pair<Mensa, QuerySnapshot>> pairs) {
                final LinkedList<Pair<Mensa, QuerySnapshot>> mensaItems = new LinkedList<>(pairs);
                Executors.defaultThreadFactory().newThread(new Runnable() {
                    @Override
                    public void run() {
                        LinkedList<MensaDay> mensaDays = new LinkedList<>();
                        LinkedList<Meal> mealsList;
                        for (Pair<Mensa, QuerySnapshot> item : mensaItems) {
                            mealsList = new LinkedList<>();
                            Mensa m = item.first;
                            QuerySnapshot qs = item.second;
                            for (DocumentSnapshot s : qs.getDocuments()) {
                                mealsList.add(s.toObject(Meal.class));
                            }
                            mensaDays.add(new MensaDay(m, mealsList));
                        }
                        mensaMealDetails.postValue(mensaDays);
                    }
                }).run();
            }
        });

    }

    public LiveData<List<Mensa>> getMensaList() {
        return mensaList;
    }
    public LiveData<List<Mensa>> getFavoriteMensaList() {
        return favoriteMensaList;
    }

    public LiveData<List<MensaDay>> getFavoriteMensaDetails() {
        return mensaMealDetails;
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
                DocumentReference mPRef = mensaSnapshot.getDocumentReference(getApplication().getString(R.string.mensa_field_mealplan_reference));
                mensa.setMealPlanReference(mPRef == null ? null : mPRef.getPath());
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

    class MensaMealPlanCreator implements Runnable {

        final List<Mensa> mensas;

        MensaMealPlanCreator(List<Mensa> mensas) {this.mensas = mensas;}

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            FirebaseFirestore instance = FirebaseFirestore.getInstance();
            WriteBatch batch = instance.batch();
            DocumentReference mensaRef, mealPlanRef;

            for (Mensa mensa : mensas) {
                mensaRef = instance.collection("Mensa").document(mensa.getuID());
                mealPlanRef = instance.collection("Mealplan").document();
                HashMap<String, Object> mealplan = new HashMap<>();
                mealplan.put("mensa", mensaRef);
                mealPlanRef.set(mealplan);
                batch.set(mealPlanRef, mealplan);
                batch.update(mensaRef, "mealplan", mealPlanRef);
            }

            batch.commit();
        }
    }

    class MensaFavoritesObserver implements Observer<List<Mensa>> {

        @Override
        public void onChanged(final List<Mensa> mensas) {

            final LocalDate localdate;
            LocalDate temp = new LocalDate();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Task<QuerySnapshot> mealplanTask;
            final LinkedList<Task<QuerySnapshot>> allMealplanTasks = new LinkedList<>();
            final LinkedList<Pair<Mensa, Task<QuerySnapshot>>> mensaMealplanTaskPairs = new LinkedList<>();
            Task<QuerySnapshot> mealsTask;
            final LinkedList<Task<QuerySnapshot>> allMealTasks = new LinkedList<>();
            final LinkedList<Pair<Mensa, Task<QuerySnapshot>>> mensaMealTaskPairs = new LinkedList<>();

            if (temp.getDayOfWeek() > 5) {
                localdate = temp.withField(DateTimeFieldType.dayOfWeek(), 5);
            } else {localdate = new LocalDate();}

            for (Mensa m : mensas) {
                if(m.getMealPlanReference() == null) {
                    continue;
                }
                mealplanTask = db.collection(m.getMealPlanReference() + "/items")
                        .whereEqualTo("year", localdate.getYear())
                        .whereEqualTo("week", localdate.getWeekOfWeekyear())
                        .get();
                allMealplanTasks.add(mealplanTask);
                mensaMealplanTaskPairs.add(new Pair<>(m, mealplanTask));
                mealsTask = db.collection("Meal")
                        .whereEqualTo("date", localdate.toDate())
                        .whereEqualTo("mensa", db.collection("Mensa").document(m.getuID()))
                        .get();
                allMealTasks.add(mealsTask);
                mensaMealTaskPairs.add(new Pair<>(m, mealsTask));
            }

            Tasks.whenAll(allMealplanTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    Mensa m;
                    Task<QuerySnapshot> fTask;
                    for (Pair<Mensa, Task<QuerySnapshot>> mtPair : mensaMealplanTaskPairs) {
                        m = mtPair.first;
                        fTask = mtPair.second;
                        Log.d(TAG, String.format("Task successful? %b", fTask.isSuccessful()));
                        if (fTask.isSuccessful()) {
                            if (fTask.getResult().getDocuments().size() == 0) {
//                                Log.d(TAG, String.format("Task with %s was empty.", fTask.getResult().getQuery().toString()));
                                Executors.defaultThreadFactory().newThread(new ApiDataLoader(getApplication().getBaseContext(), m)).run();
                            } else {
                                for (DocumentSnapshot e : fTask.getResult().getDocuments()) {
//                                    Log.d(TAG, String.format("Mealplan %s was found.", e.getId()));
                                }
                            }
                        }
                    }
                }
            });

            Tasks.whenAll(allMealTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                Mensa m;
                Task<QuerySnapshot> fTask;
                LinkedList<Pair<Mensa, QuerySnapshot>> results = new LinkedList<>();
                for (Pair<Mensa, Task<QuerySnapshot>> mensaTaskPair : mensaMealTaskPairs) {
                    m = mensaTaskPair.first;
                    fTask = mensaTaskPair.second;
                    if (fTask.isSuccessful()) {
                        results.add(new Pair<>(m, fTask.getResult()));
                    }
                }

                mensaMealsSnapshots.postValue(results);


                }
            });



        }

    }



}