package com.miage.covidair.service;

import android.os.AsyncTask;
import android.util.Log;

import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.model.Location;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocationSearchService {
    public static LocationSearchService INSTANCE = new LocationSearchService();

    private LocationSearchService() {

    }

    public void searchFromAPI(String city) {
        // Create AsyncTask
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                // Here we are in a new background thread
                try {
                    final OkHttpClient okHttpClient = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url("https://api.openaq.org/v1/locations?city="+city+"&country=FR&limit=10000")
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    if (response != null && response.body() != null) {
                        JSONObject jsonResult = new JSONObject(response.body().string());
                        JSONArray jsonLocations = jsonResult.getJSONArray("results");

                        List<Location> foundLocations = new ArrayList<>();
                        for (int i = 0; i < jsonLocations.length(); i++) {
                            JSONObject jsonLocation = jsonLocations.getJSONObject(i);
                            String actualCity = jsonLocation.getString("city");
                            if (city.equals(actualCity)){
                                String location = jsonLocation.getString("location");
                                String count = jsonLocation.getString("count");
                                String lastUpdated = jsonLocation.getString("lastUpdated").substring(0,10)
                                        + " " + jsonLocation.getString("lastUpdated").substring(11,19);  //TODO Joda Time ISODateTimeFormat.dateTime()
                                if (!foundLocations.isEmpty()){
                                    if(!foundLocations.get(foundLocations.size()-1).getLocation().equals(location)) {
                                        foundLocations.add(new Location(location, count, lastUpdated));
                                    }
                                } else {
                                    foundLocations.add(new Location(location, count, lastUpdated));
                                }
                            }
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