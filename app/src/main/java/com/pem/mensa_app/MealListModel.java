package com.pem.mensa_app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.pem.mensa_app.models.meal.Meal;

import org.joda.time.DateTimeFieldType;
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

public class MealListModel extends AndroidViewModel {

    private final MutableLiveData<LinkedList<Meal>> mealData = new MutableLiveData<>();
    private ArrayList<LinkedList<Meal>> weekMealData;
    private int selectedWeekday = -1;

    private static final String EAT_API_URL_FORMAT = "https://srehwald.github.io/eat-api/%s/%d/%d.json";
    private static final String TAG = MealListModel.class.getSimpleName();

    public MealListModel(@NonNull Application application) {
        super(application);
        mealData.setValue(new LinkedList<Meal>());
    }

    public MutableLiveData<LinkedList<Meal>> getMealData() {
        return mealData;
    }

    @SuppressLint("DefaultLocale")
    private static String generateEatApiUrl(String eatApiMensaString, LocalDate date){
        return String.format(EAT_API_URL_FORMAT, eatApiMensaString, date.getYear(), date.getWeekOfWeekyear());
    }

    private String mensaID;
    private String mensaName;
    private String mealPlanReferencePath;
    private String mensaEatApiUrl;

    public void informationSet(){
        LocalDate date = new LocalDate(DateTimeZone.forID("Europe/Berlin"));
        if(selectedWeekday == -1) {
            // Starts with monday = 0, therefore subtract 1
            selectedWeekday = date.dayOfWeek().get() -1 ;
            if (selectedWeekday > 4) {
                selectedWeekday = 4;
            }
        }
        if (mensaEatApiUrl != null) {
            if (mealPlanReferencePath != null) {
                loadDataFromFirebase(date);
            } else {
                checkMealPlanReferenceExists(mensaID);
            }
        } else {
            // HANDLE MISSING API SUPPORT.
        }
    }

    private void parseMealData(DocumentSnapshot mealPlan, QuerySnapshot mealSnapshot) {

        ArrayList<LinkedList<Meal>> meals = new ArrayList<>(Arrays.asList(new LinkedList<Meal>(), new LinkedList<Meal>(), new LinkedList<Meal>(), new LinkedList<Meal>(), new LinkedList<Meal>()));
        int weekday; Meal meal;
        for(DocumentSnapshot snapshot : mealSnapshot) {
            meal = new Meal();
            // price?
            meal.setName(snapshot.getString(getString(R.string.meal_field_name)));
            weekday = snapshot.getDouble("weekday").intValue();
            meal.setIngredients((ArrayList<String>) snapshot.get("ingredients"));
            meal.setImages((ArrayList<String>) snapshot.get("imagePaths"));
            meal.setUid(snapshot.getId());
            meals.get(weekday).add(meal);

        }

        weekMealData = meals;
        mealData.postValue(weekMealData.get(this.selectedWeekday));
    }

    private void loadDataFromFirebase(LocalDate date) {
        final LocalDate mDate = new LocalDate(date);
        FirebaseFirestore.getInstance().collection(mealPlanReferencePath + "/items")
                .whereEqualTo(getApplication().getString(R.string.mealplan_field_year), date.year().get())
                .whereEqualTo(getApplication().getString(R.string.mealplan_field_week), date.weekOfWeekyear().get())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            //todo surface items
                            Log.d(TAG, "loadDataFromFirebase complete and successful");
                            FirebaseFirestore instance = FirebaseFirestore.getInstance();
                            final DocumentSnapshot mealplan = task.getResult().getDocuments().get(0);
                            instance.collection(getString(R.string.meal_collection_identifier))
                                    .whereEqualTo("mealplan", instance.collection(mealPlanReferencePath + "/items").document(mealplan.getId()))
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful() && !task.getResult().isEmpty()) {
                                        Log.d(TAG, "load meal data complete and successful.");
                                        Log.d(TAG, String.format("result is %d large", task.getResult().size()));
                                        parseMealData(mealplan, task.getResult());
                                    }
                                }
                            });


                        } else if (task.getResult().isEmpty()) {
                            Log.d(TAG, "No mealplan for selected week found. Generating.");
                            loadDataFromEatApi(new LocalDate(mDate));
                        }
                    }
                });

    }

    private void loadDataFromEatApi(final LocalDate date) {
        final String requestUrl = generateEatApiUrl(mensaEatApiUrl, date);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, String.format("Retrieved data from %s", requestUrl));
                        createMealplanOnFirebase(date, response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, String.format("Error retrieving data from %s", requestUrl));

                    }
                });
        Volley.newRequestQueue(getApplication()).add(jsonObjectRequest);

    }

    private void createMealplanOnFirebase(LocalDate date, JSONObject mealPlanData){
        DocumentReference newMealplanRef = FirebaseFirestore.getInstance().collection(mealPlanReferencePath + "/items").document();
        DocumentReference mensaRef = FirebaseFirestore.getInstance().collection(getString(R.string.mensa_collection_identifier)).document(mensaID);
        Map<String, Object> firebaseData = new HashMap<>();
        firebaseData.put(getString(R.string.mealplan_field_week), date.weekOfWeekyear().get());
        firebaseData.put(getString(R.string.mealplan_field_year), date.year().get());

        ArrayList<Map<String, Object>> daysList = new ArrayList<>(Arrays.<Map<String, Object>>asList(null, null, null, null, null));
        LinkedList<Meal> dishesList;
        Map<String, Object> dayMap;
        JSONObject day, dish; String dishname; double dishprice;
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
                int weekday = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dateString).dayOfWeek().get() - 1;
                for(int j = 0; j < dishes.length(); j++) {
                    dish = dishes.getJSONObject(j);
                    dishname = dish.getString("name");
//                    dishprice = dish.getDouble("price");
                    ingredients = new ArrayList<>();
                    ingredientsJson = dish.getJSONArray("ingredients");
                    for(int k = 0; k < ingredientsJson.length(); k++){
                        ingredient = ingredientsJson.getString(k);
                        ingredients.add(ingredient);
                    }
                    meal = new Meal(null, dishname, null,  weekday, null, null, ingredients, null, null, 0);
                    dishesList.add(meal);
                }
                date.withField(DateTimeFieldType.dayOfWeek(), weekday + 1);
                dayMap.put(getString(R.string.mealplan_field_meals), getReferenceListFromMeals(dishesList, newMealplanRef, date, weekday));
                daysList.set(weekday, dayMap);
            }
            firebaseData.put(getString(R.string.mealplan_field_days), daysList);
            firebaseData.put("mensa", mensaRef);
            newMealplanRef.set(firebaseData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Successfully loaded eatapi data to firebase.");
                                informationSet();
                            } else {
                                Log.d(TAG, "Failed to load eatapi data to firebase");
                            }
                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkMealPlanReferenceExists(final String mensaID) {
        FirebaseFirestore instance = FirebaseFirestore.getInstance();
        instance.collection(getString(R.string.mensa_collection_identifier)).document(mensaID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    if (task.getResult().getDocumentReference(getString(R.string.mensa_field_mealplan_reference)) != null) {
                        mealPlanReferencePath = task.getResult().getDocumentReference(getString(R.string.mensa_field_mealplan_reference)).getPath();
                        informationSet();
                    } else {
                        createMealplanReferenceOnFirebase(mensaID);
                    }
                }
            }
        });
    }

    private void createMealplanReferenceOnFirebase(String mensaID) {
        FirebaseFirestore instance = FirebaseFirestore.getInstance();
        final DocumentReference mensaRef = instance.collection(getString(R.string.mensa_collection_identifier)).document(mensaID);
        final DocumentReference mealplanRef = instance.collection(getString(R.string.mealplan_collection_identifier)).document(mensaID);
        HashMap<String, Object> mealplan = new HashMap<>();
        mealplan.put("mensa", mensaRef);
        mealplanRef.set(mealplan, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, String.format("Added Mealplan %s on Firebase", mealplanRef.getPath()));
                    mealPlanReferencePath = mealplanRef.getPath();
                    mensaRef.update(getString(R.string.mensa_field_mealplan_reference), mealplanRef);
                    informationSet();
                } else {
                    Log.d(TAG, "Error creating Mealplan on Firebase");
                }
            }
        });
    }

    private List<DocumentReference> getReferenceListFromMeals(List<Meal> mealList, final DocumentReference mealPlanRef, LocalDate date, int weekday){
        CollectionReference mealRef = FirebaseFirestore.getInstance().collection(getString(R.string.meal_collection_identifier));
        DocumentReference mensaRef = FirebaseFirestore.getInstance().collection(getString(R.string.mensa_collection_identifier)).document(mensaID);
        DocumentReference docRef;
        Timestamp timestamp = new Timestamp(date.toDate());
        LinkedList<DocumentReference> references = new LinkedList<>();
        List<String> ingredients;
        List<String> imagePaths = new ArrayList<>();
        HashMap<String, Object> mealMap;

        for(Meal meal : mealList) {
            mealMap = new HashMap<>();
            ingredients = meal.getIngredients();

            mealMap.put(getString(R.string.meal_field_name), meal.getName());
            mealMap.put(getString(R.string.meal_field_price), meal.getPrice());

            mealMap.put(getString(R.string.meal_field_ingredients), ingredients);
            mealMap.put("mealplan", mealPlanRef);
            mealMap.put("weekday", weekday);
            mealMap.put("imagePaths", imagePaths);
            mealMap.put("mensa", mensaRef);
            mealMap.put("date", timestamp);

            docRef = mealRef.document();
            final String mealName = meal.getName();
            final String refPath = docRef.getPath();
            docRef.set(mealMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, String.format("Added meal %s at %s", mealName, refPath));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, String.format("Failed to add meal %s at %s", mealName, refPath));
                }
            });
            references.add(docRef);
        }

        return references;

    }

    public void setSelectedWeekday(int weekday){
        this.selectedWeekday = weekday;
        if(weekMealData != null) {
            mealData.postValue(weekMealData.get(selectedWeekday - 1));
        }
    }

    public void setData(Bundle data) {
        setMensaName(data.getString(getString(R.string.intent_mensa_name)));
        setMensaID(data.getString(getString(R.string.intent_mensa_uid)));
        setMealPlanReferencePath(data.getString(getString(R.string.intent_mensa_meal_plan_reference_path)));
        setMensaEatApiUrl(data.getString(getString(R.string.intent_mensa_eatapi_url)));
    }

    public int getSelectedWeekday() {
        return selectedWeekday;
    }

    private String getString(int id) {
        return getApplication().getString(id);
    }

    private void setMensaID(String mensaID) {
        this.mensaID = mensaID;
    }

    private void setMensaName(String mensaName) {
        this.mensaName = mensaName;
    }

    public String getMensaName() {
        return mensaName;
    }

    public String getMealPlanReferencePath() {
        return this.mealPlanReferencePath;
    }

    private void setMealPlanReferencePath(String mealPlanReferencePath) {
        this.mealPlanReferencePath = mealPlanReferencePath;
    }

    private void setMensaEatApiUrl(String mensaEatApiUrl) {
        this.mensaEatApiUrl = mensaEatApiUrl;
    }


}
