package com.pem.mensa_app.utilities.eatapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.pem.mensa_app.models.meal.Meal;
import com.pem.mensa_app.models.mensa.Mensa;
import com.pem.mensa_app.ui.home_activity.HomeViewModel;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultiApiDataLoader implements Runnable {
    
    private static final String TAG = MultiApiDataLoader.class.getSimpleName();

    private static final String EAT_API_URL_FORMAT = "https://srehwald.github.io/eat-api/%s/%d/%d.json";

    @SuppressLint("DefaultLocale")
    private String generateEatApiUrl(String eatApiMensaString, LocalDate date){
        return String.format(EAT_API_URL_FORMAT, eatApiMensaString, date.getYear(), date.getWeekOfWeekyear());
    }
    
    final List<Mensa> mensaList;
    final LocalDate date;
    Context context;
    LinkedList<Task<Void>> batches = new LinkedList<>();
    int requests;
    final RequestQueue queue;
    final HomeViewModel.DatabaseState state;
    
    public MultiApiDataLoader(Context context, List<Mensa> mensas, HomeViewModel.DatabaseState state) {
        this.context = context;
        this.mensaList = mensas;
        this.date = new LocalDate(DateTimeZone.forID("Europe/Berlin"));
        this.requests = mensaList.size();
        this.state = state;
        this.queue = Volley.newRequestQueue(context);
        queue.start();
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        for (final Mensa m : mensaList) {
            final String requestUrl = generateEatApiUrl(m.getUrl(), date);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    batches.add(createMealplanOnFirebase(date, response, m));
                    decrementRequestCounter();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, String.format("Error retrieving data from %s", requestUrl));
                    Log.d(TAG, error.getCause().getLocalizedMessage());
                }
            }
            );
            Volley.newRequestQueue(context).add(jsonObjectRequest);
        }
    }

    private Task createMealplanOnFirebase(LocalDate date, JSONObject mealPlanData, Mensa mensa) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();
        DocumentReference newMealplanRef = db.collection(mensa.getMealPlanReference() + "/items").document();
        Map<String, Object> mealplanData = new HashMap<>();
        mealplanData.put("week", date.weekOfWeekyear().get());
        mealplanData.put("year", date.year().get());

        ArrayList<Map<String, Object>> daysList = new ArrayList<>(Arrays.<Map<String, Object>>asList(null, null, null, null, null));
        LinkedList<Meal> dishesList;
        Map<String, Object> dayMap;
        JSONObject day, dish; String dishname;
        JSONArray dishes, ingredientsJson;
        ArrayList<String> ingredients;
        String ingredient;
        try {
            JSONArray daysArray = mealPlanData.getJSONArray("days");
            Meal meal;
            for(int i = 0; i < daysArray.length(); i++){
                dayMap = new HashMap<>();
                dishesList = new LinkedList<>();
                day = daysArray.getJSONObject(i);
                dishes = day.getJSONArray("dishes");
                String dateString = day.getString("date");
                LocalDate dayDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate(dateString);
                int weekday = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dateString).dayOfWeek().get() - 1;
                for(int j = 0; j < dishes.length(); j++) {
                    dish = dishes.getJSONObject(j);
                    dishname = dish.getString("name");
                    ingredients = new ArrayList<>();
                    ingredientsJson = dish.getJSONArray("ingredients");
                    for(int k = 0; k < ingredientsJson.length(); k++){
                        ingredient = ingredientsJson.getString(k);
                        ingredients.add(ingredient);
                    }
                    meal = new Meal();
                    meal.setName(dishname);
                    meal.setWeekday(weekday);
                    meal.setIngredients(ingredients);
                    dishesList.add(meal);
                }
                dayMap.put("meals", getReferenceListFromMeals(dishesList, mensa, newMealplanRef, dayDate, weekday, batch));
                daysList.set(weekday, dayMap);
            }
            mealplanData.put("days", daysList);
            batch.set(newMealplanRef, mealplanData);
            return batch.commit();

        } catch (JSONException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("couldn't parse json");
        }
    }

    private List<DocumentReference> getReferenceListFromMeals(List<Meal> mealList, Mensa mensa, final DocumentReference mealPlanRef, LocalDate date, int weekday, WriteBatch batch){
        CollectionReference mealRef = FirebaseFirestore.getInstance().collection("Meal");
        DocumentReference mensaRef = FirebaseFirestore.getInstance().collection("Mensa").document(mensa.getuID());
        DocumentReference docRef;
        LinkedList<DocumentReference> references = new LinkedList<>();
        List<String> ingredients;
        List<String> imagePaths = new ArrayList<>();
        HashMap<String, Object> mealMap;
        Timestamp timestamp = new Timestamp(date.toDate());


        for(Meal meal : mealList) {
            mealMap = new HashMap<>();
            ingredients = meal.getIngredients();

            mealMap.put("name", meal.getName());
            mealMap.put("price", meal.getPrice());

            mealMap.put("ingredients", ingredients);
            mealMap.put("mealplan", mealPlanRef);
            mealMap.put("weekday", weekday);
            mealMap.put("imagePaths", imagePaths);
            mealMap.put("mensa", mensaRef);
            mealMap.put("date", timestamp);

            docRef = mealRef.document();
            batch.set(docRef, mealMap);
            references.add(docRef);
        }

        return references;

    }

    private void decrementRequestCounter() {
        this.requests--;
        if (requests == 0) {
            commitBatch();
        }
    }

    private void commitBatch() {
        Tasks.whenAll(batches).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                state.apiLoaderComplete();
            }
        });
    }
}
