package com.miage.covidair.service;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchLatestMeasurementsResultEvent;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.model.Location.Latest;
import com.miage.covidair.model.Location.LatestSearchResult;
import com.miage.covidair.model.Location.Loca;
import com.miage.covidair.model.Location.LocationSearchResult;
import com.miage.covidair.model.Location.Measurement;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationSearchService {
    public static LocationSearchService INSTANCE = new LocationSearchService();
    public ISearchRESTService mISearchRESTService;
    private static final long REFRESH_DELAY = 650;
    private ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mLastScheduleTask;

    private LocationSearchService() {
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
        mISearchRESTService = retrofit.create(ISearchRESTService.class);
    }

    public void searchLocations(String city) {
    // Cancel last scheduled network call (if any)
        if (mLastScheduleTask != null && !mLastScheduleTask.isDone()) {
            mLastScheduleTask.cancel(true);
        }
        // Schedule a network call in REFRESH_DELAY ms
        mLastScheduleTask = mScheduler.schedule(() -> {
            // Step 1 : first run a local search from DB and post result
            searchLocationsFromDB(city);

            // Step 2 : Call to the REST service
            mISearchRESTService.searchForLocations("FR", city, 10000).enqueue(new Callback<LocationSearchResult>() {
                @Override
                public void onResponse(Call<LocationSearchResult> call, retrofit2.Response<LocationSearchResult> response) {
                    // Post an event so that listening activities can update their UI
                    if (response.body() != null && response.body().results != null) {
                        // Save all results in Database
                        ActiveAndroid.beginTransaction();
                        for (Loca loca : response.body().results) {
                            //TODO Joda Time ISODateTimeFormat.dateTime()
                            //TODO : test sur la date pour last update
                            if (city.equals(loca.city)){
                                loca.coordinates.location = loca.location;
                                loca.save();
                                loca.coordinates.save();
                            }
                        }
                        ActiveAndroid.setTransactionSuccessful();
                        ActiveAndroid.endTransaction();
                        // Send a new event with results from network
                        searchLocationsFromDB(city);

                        searchLatestMeasurements(city);

                    } else {
                        // Null result
                        // We may want to display a warning to user (e.g. Toast)

                        Log.e("[CovidAir] [REST]", "Response error : null body");
                    }
                }

                @Override
                public void onFailure(Call<LocationSearchResult> call, Throwable t) {
                    // Request has failed or is not at expected format
                    // We may want to display a warning to user (e.g. Toast)
                    Log.e("[CovidAir] [REST]", "Response error : " + t.getMessage());
                }
            });
        }, REFRESH_DELAY, TimeUnit.MILLISECONDS);

    }

    public void searchLatestMeasurements(String city) {
        List<Loca> matchingLocationsFromDB = new Select()
                .from(Loca.class)
                .where("city LIKE '%" + city + "%'")
                .orderBy("location")
                .execute();

        for (Loca loca : matchingLocationsFromDB){
            if (mLastScheduleTask != null && !mLastScheduleTask.isDone()) {
                mLastScheduleTask.cancel(true);
            }
            mLastScheduleTask = mScheduler.schedule(() -> {
                // Step 1 : first run a local search from DB and post result
                searchLatestMeasurementsFromDB(loca.city,loca.location,"FR");

                // Step 2 : Call to the REST service
                mISearchRESTService.searchForLatest("FR", city, loca.location).enqueue(new Callback<LatestSearchResult>() {
                    @Override
                    public void onResponse(Call<LatestSearchResult> call, Response<LatestSearchResult> response) {
                        // Post an event so that listening activities can update their UI
                        if (response.body() != null && response.body().results != null) {
                            // Save all results in Database
                            ActiveAndroid.beginTransaction();
                            for (Latest latest : response.body().results) {
                                latest.save();

                            }
                            ActiveAndroid.setTransactionSuccessful();
                            ActiveAndroid.endTransaction();
                            // Send a new event with results from network
                            searchLatestMeasurementsFromDB(loca.city,loca.location,"FR");

                        } else {
                            // Null result
                            // We may want to display a warning to user (e.g. Toast)

                            Log.e("[CovidAir] [REST]", "Response error : null body");
                        }
                    }

                    @Override
                    public void onFailure(Call<LatestSearchResult> call, Throwable t) {
                        // Request has failed or is not at expected format
                        // We may want to display a warning to user (e.g. Toast)
                        Log.e("[CovidAir] [REST]", "Response error : " + t.getMessage());
                    }
                });
            }, REFRESH_DELAY, TimeUnit.MILLISECONDS);


        }

    }

    private void searchLocationsFromDB(String city) {
        List<Loca> matchingLocationsFromDB = new Select()
                .from(Loca.class)
                .where("city LIKE '%" + city + "%'")
                .orderBy("location")
                .execute();
        EventBusManager.BUS.post(new SearchLocationResultEvent(matchingLocationsFromDB));
    }

    private void searchLatestMeasurementsFromDB(String city, String location, String country) {
        List<Measurement> matchingLatestMeasurementsFromDB = new Select()
                .from(Measurement.class)
                .where("key LIKE '%" + city+location+country+"%'")
                .orderBy("parameter")
                .execute();
        EventBusManager.BUS.post(new SearchLatestMeasurementsResultEvent(matchingLatestMeasurementsFromDB));
    }
}