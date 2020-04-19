package com.miage.covidair.service;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.model.Location.Latest;
import com.miage.covidair.model.Location.LatestSearchResult;
import com.miage.covidair.model.Location.Loca;
import com.miage.covidair.model.Location.LocationSearchResult;
import com.miage.covidair.model.Measurement.Measurement;

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResponse(Call<LocationSearchResult> call, retrofit2.Response<LocationSearchResult> response) {
                    // Post an event so that listening activities can update their UI
                    if (response.body() != null && response.body().results != null) {
                        // Save all results in Database
                        ActiveAndroid.beginTransaction();
                        for (Loca loca : response.body().results) {
                            //TODO : test sur la date pour last update

                            if (city.equals(loca.city)) {
                                    loca.coordinates.location = loca.location; //TODO: INUTILE
                                    loca.longitude = loca.coordinates.longitude;
                                    loca.latitude = loca.coordinates.latitude;
                                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.FRENCH);
                                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
                                    LocalDate date = LocalDate.parse(loca.lastUpdated, inputFormatter);
                                    String formattedDate = outputFormatter.format(date);
                                    loca.lastUpdated = formattedDate;
                                    loca.latestMeasurements = new HashMap<>();
                                    loca.save();

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
                        Log.e("[CovidAir] [LOCATION] [REST]", "Response error : null body");
                    }
                }


                @Override
                public void onFailure(Call<LocationSearchResult> call, Throwable t) {
                    // Request has failed or is not at expected format
                    // We may want to display a warning to user (e.g. Toast)
                    Log.e("[CovidAir] [LOCATION] [REST]", "Response error : " + t.getMessage());
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
                searchLocationsFromDB(city);
                //searchLatestMeasurementsFromDB(loca.city,loca.location,"FR");

                // Step 2 : Call to the REST service
                mISearchRESTService.searchForLatest(loca.latitude+","+loca.longitude,750,10000).enqueue(new Callback<LatestSearchResult>() {
                    @Override
                    public void onResponse(Call<LatestSearchResult> call, Response<LatestSearchResult> response) {
                        // Post an event so that listening activities can update their UI
                        if (response.body() != null && response.body().results != null) {
                            // Save all results in Database
                            ActiveAndroid.beginTransaction();
                            HashMap<String,Measurement> latestMeasurements = new HashMap<>();
                            for (Latest latest : response.body().results) {
                                for (Measurement measurement : latest.measurements) {
                                    latestMeasurements.put(measurement.parameter,measurement);
                                }
                            }
                            loca.setLatestMeasurements(latestMeasurements);
                            loca.save();

                            ActiveAndroid.setTransactionSuccessful();
                            ActiveAndroid.endTransaction();
                            searchLocationsFromDB(city);
                        } else {
                            // Null result
                            // We may want to display a warning to user (e.g. Toast)

                            Log.e("[CovidAir] [LATEST] [REST]", "Response error : null body");
                        }
                    }

                    @Override
                    public void onFailure(Call<LatestSearchResult> call, Throwable t) {
                        // Request has failed or is not at expected format
                        // We may want to display a warning to user (e.g. Toast)
                        Log.e("[CovidAir] [LATEST] [REST]", "Response error : " + t.getMessage());
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

    private HashMap<String,Measurement> searchLatestMeasurementsFromDB(String location) {
        String[] params = {"pm25", "pm10", "so2", "no2", "o3", "co", "bc"};
        HashMap<String,Measurement> latestMeasurementsFromDB = new HashMap<>();
        for (String parameter : params){
            List<Measurement> matchingLatest = new Select()
                    .from(Measurement.class)
                    .where("location LIKE '%" +location+"%' AND parameter LIKE'%"+ parameter + "%'")
                    .orderBy("date DESC")
                    .limit(1)
                    .execute();
            if (matchingLatest != null && !matchingLatest.isEmpty()){
                latestMeasurementsFromDB.put(matchingLatest.get(0).parameter,matchingLatest.get(0));
            }
        }
        return latestMeasurementsFromDB;
    }
}