package com.pem.mensa_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pem.mensa_app.models.meal.Meal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MensaMealListActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private MealListAdapter mMealListAdapter;
    private ArrayList<Meal> mMealList;
    private RequestQueue mRequestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensa_meal_list);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMealList = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(this);
        parseJSON();

    }

    private void parseJSON(){


        String url = "https://srehwald.github.io/eat-api/mensa-garching/2019/24.json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            mMealList = new ArrayList<Meal>();
                            //loop in days
                            JSONArray jsonArray = response.getJSONArray("days");
                            for (int i=0;i<jsonArray.length();i++){
                                JSONObject day = jsonArray.getJSONObject(i);

                                String dishesarray = day.getString("dishes");
                                String date = day.getString("date");


                                //loop in dishes
                                JSONArray JsonDishes = new JSONArray(dishesarray);
                                for (int j=0;j<(JsonDishes.length());j++){
                                    JSONObject json_obj=JsonDishes.getJSONObject(j);

                                    String namedishes=json_obj.getString("name");
                                    Double dishes_price=json_obj.getDouble("price");

                                   // mMealList.add(new Meal(namedishes,dishes_price));

                                }

                            }

                            mMealListAdapter = new MealListAdapter(MensaMealListActivity.this, mMealList);
                            mRecyclerView.setAdapter(mMealListAdapter);

                        } catch (JSONException e)
                        {

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }

}
