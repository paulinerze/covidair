package com.miage.covidair;

import android.os.AsyncTask;
import android.util.Log;

import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchResultEvent;
import com.miage.covidair.model.City;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CitySearchService {

    public static CitySearchService INSTANCE = new CitySearchService();

    private CitySearchService() {

    }

    public void searchFromAPI(final String search) {
        // Create AsyncTask
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                // Here we are in a new background thread
                try {
                    final OkHttpClient okHttpClient = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url("https://api.openaq.org/v1/"+ search)
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    if (response != null && response.body() != null) {
                        JSONObject jsonResult = new JSONObject(response.body().string());
                        JSONArray jsonCities = jsonResult.getJSONArray("results");

                        List<City> foundCities = new ArrayList<>();
                        for (int i = 0; i < jsonCities.length(); i++) {
                            JSONObject jsonCity = jsonCities.getJSONObject(i);
                            String name = jsonCity.getString("name");
                            String count = jsonCity.getString("count");
                            String locations = jsonCity.getString("locations");
                            foundCities.add(new City(name,count,locations));
                        }
                        EventBusManager.BUS.post(new SearchResultEvent(foundCities));
                    }
                } catch (IOException e) {
                    // Silent catch, no cities will be displayed
                    Log.e("CovidAir - Network Issue", e.getMessage());
                } catch (JSONException e) {
                    // Silent catch, no cities will be displayed
                    Log.e("CovidAir - Json Exception", e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}
