package com.miage.covidair.service;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchDetailResultEvent;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.model.Detail;
import com.miage.covidair.model.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailSearchService {
    public static DetailSearchService INSTANCE = new DetailSearchService();

    private DetailSearchService() {

    }

    public void searchFromAPI(String city, String location) {
        // Create AsyncTask
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected Void doInBackground(Void... params) {
                // Here we are in a new background thread
                try {
                    final OkHttpClient okHttpClient = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url("https://api.openaq.org/v1/measurements?city="+city+"&location="+location+"&country=FR&limit=10000")
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    if (response != null && response.body() != null) {
                        JSONObject jsonResult = new JSONObject(response.body().string());
                        JSONArray jsonLocations = jsonResult.getJSONArray("results");

                        List<Detail> foundDetails = new ArrayList<>();
                        for (int i = 0; i < jsonLocations.length(); i++) {
                            JSONObject jsonDetail = jsonLocations.getJSONObject(i);
                            String parameter = jsonDetail.getString("parameter");
                            String value = jsonDetail.getString("value");
                            String unit = jsonDetail.getString("unit");
                            JSONObject jsonDate = jsonDetail.getJSONObject("date");
                            String localDate = jsonDate.getString("local").substring(0,10)
                                    + " " + jsonDate.getString("local").substring(11,19);
                            JSONObject jsonCoordinate = jsonDetail.getJSONObject("coordinates");
                            String longitude = jsonCoordinate.getString("longitude");
                            String latitude = jsonCoordinate.getString("latitude");
                            foundDetails.add(new Detail(parameter,value,unit,localDate,longitude,latitude));
                        }
                        EventBusManager.BUS.post(new SearchDetailResultEvent(foundDetails));
                    }
                } catch (IOException e) {
                    // Silent catch, no details will be displayed
                    Log.e("CovidAir - Network Issue", e.getMessage());
                } catch (JSONException e) {
                    // Silent catch, no details will be displayed
                    Log.e("CovidAir - Json Exception", e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}