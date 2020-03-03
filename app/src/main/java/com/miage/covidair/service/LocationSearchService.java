package com.miage.covidair.service;

import android.os.AsyncTask;
import android.util.Log;

import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchCityResultEvent;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.model.City;
import com.miage.covidair.model.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocationSearchService {
    public static LocationSearchService INSTANCE = new LocationSearchService();

    private LocationSearchService() {

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
                        JSONArray jsonLocations = jsonResult.getJSONArray("results");

                        List<Location> foundLocations = new ArrayList<>();
                        for (int i = 0; i < jsonLocations.length(); i++) {
                            JSONObject jsonCity = jsonLocations.getJSONObject(i);
                            String location = jsonCity.getString("location");
                            String count = jsonCity.getString("count");
                            String lastUpdated = jsonCity.getString("lastUpdated");
                            foundLocations.add(new Location(location,count,lastUpdated));
                        }
                        EventBusManager.BUS.post(new SearchLocationResultEvent(foundLocations));
                    }
                } catch (IOException e) {
                    // Silent catch, no locations will be displayed
                    Log.e("CovidAir - Network Issue", e.getMessage());
                } catch (JSONException e) {
                    // Silent catch, no locations will be displayed
                    Log.e("CovidAir - Json Exception", e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}