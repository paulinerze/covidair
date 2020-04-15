package com.miage.covidair.service;

import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miage.covidair.domain.ICitySearchRESTService;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchCityResultEvent;
import com.miage.covidair.model.City;
import com.miage.covidair.model.CitySearchResult;

import org.json.JSONArray;
import org.json.JSONException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.converter.gson.GsonConverterFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

public class CitySearchService {
    public ICitySearchRESTService mICitySearchRESTService;

    public static CitySearchService INSTANCE = new CitySearchService();
    private static final long REFRESH_DELAY = 650;
    private ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mLastScheduleTask;

    private CitySearchService() {
        // Create GSON Converter that will be used to convert from JSON to Java
        Gson gsonConverter = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation().create();

        // Create Retrofit client
        Retrofit retrofit = new Retrofit.Builder()
                // Using OkHttp as HTTP Client
                .client(new OkHttpClient())
                // Having the following as server URL
                .baseUrl("https://api.openaq.org/v1/")
                // Using GSON to convert from Json to Java
                .addConverterFactory(GsonConverterFactory.create(gsonConverter))
                .build();

        // Use retrofit to generate our REST service code
        mICitySearchRESTService = retrofit.create(ICitySearchRESTService.class);
    }

    public void searchFromAPI() {
        // Cancel last scheduled network call (if any)
        if (mLastScheduleTask != null && !mLastScheduleTask.isDone()) {
            mLastScheduleTask.cancel(true);
        }
        // Schedule a network call in REFRESH_DELAY ms
        mLastScheduleTask = mScheduler.schedule(() -> {
            // Step 1 : first run a local search from DB and post result
            //searchCitiesFromDB();

            // Step 2 : Call to the REST service
            mICitySearchRESTService.searchForCities("FR",10000).enqueue(new Callback<CitySearchResult>() {
                @Override
                public void onResponse(Call<CitySearchResult> call, retrofit2.Response<CitySearchResult> response) {
                    // Post an event so that listening activities can update their UI
                    if (response.body() != null && response.body().results != null) {
                        // Save all results in Database
                        ActiveAndroid.beginTransaction();
                        for (City city : response.body().results) {
                            city.save();
                        }
                        ActiveAndroid.setTransactionSuccessful();
                        ActiveAndroid.endTransaction();

                        // Send a new event with results from network
                        searchCitiesFromDB();
                    } else {
                        // Null result
                        // We may want to display a warning to user (e.g. Toast)

                        Log.e("[CovidAir] [REST]", "Response error : null body");
                    }
                }

                @Override
                public void onFailure(Call<CitySearchResult> call, Throwable t) {
                    // Request has failed or is not at expected format
                    // We may want to display a warning to user (e.g. Toast)
                    Log.e("[CovidAir] [REST]", "Response error : " + t.getMessage());
                }
            });
        }, REFRESH_DELAY, TimeUnit.MILLISECONDS);

        /*
        // Create AsyncTask
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                // Here we are in a new background thread
                try {
                    final OkHttpClient okHttpClient = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url("https://api.openaq.org/v1/cities?country=FR&limit=10000")
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
                        EventBusManager.BUS.post(new SearchCityResultEvent(foundCities));
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
        }.execute();*/
    }

    private void searchCitiesFromDB() {
        List<City> matchingCitiesFromDB = new Select().from(City.class).orderBy("name").execute();
        EventBusManager.BUS.post(new SearchCityResultEvent(matchingCitiesFromDB));
    }
}
