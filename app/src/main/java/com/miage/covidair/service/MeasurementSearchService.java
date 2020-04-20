package com.miage.covidair.service;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchMeasurementResultEvent;
import com.miage.covidair.model.Measurement.Measurement;
import com.miage.covidair.model.Measurement.MeasurementSearchResult;

import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MeasurementSearchService {
    public static MeasurementSearchService INSTANCE = new MeasurementSearchService();
    public ISearchRESTService mISearchRESTService;
    private static final long REFRESH_DELAY = 650;
    private ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mLastScheduleTask;

    private MeasurementSearchService() {
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

    public void searchDetails(String city, String location, String longitude, String latitude) {
        // Cancel last scheduled network call (if any)
        if (mLastScheduleTask != null && !mLastScheduleTask.isDone()) {
            mLastScheduleTask.cancel(true);
        }
        // Schedule a network call in REFRESH_DELAY ms
        mLastScheduleTask = mScheduler.schedule(() -> {
            // Step 1 : first run a local search from DB and post result
            searchLatestMeasurementsFromDB(location, city);

            // Step 2 : Call to the REST service
            mISearchRESTService.searchForMeasurements(latitude+","+longitude,750,30).enqueue(new Callback<MeasurementSearchResult>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResponse(Call<MeasurementSearchResult> call, retrofit2.Response<MeasurementSearchResult> response) {
                    // Post an event so that listening activities can update their UI
                    if (response.body() != null && response.body().results != null) {
                        // Save all results in Database
                        ActiveAndroid.beginTransaction();
                        for (Measurement measurement : response.body().results) {
                            //TODO : test sur la date pour last update
                            if (location.equals(measurement.location) && city.equals(measurement.city)) {
                                measurement.latitude = measurement.coordinates.latitude;
                                measurement.longitude = measurement.coordinates.longitude;
                                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.FRENCH);
                                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                String strDate = dateFormat.format(measurement.date.utc);
                                LocalDate date = LocalDate.parse(strDate, inputFormatter);
                                String formattedDate = outputFormatter.format(date);
                                measurement.displayDate = formattedDate;
                                measurement.orderDate = measurement.date.utc;
                                measurement.save();
                            }
                        }
                        ActiveAndroid.setTransactionSuccessful();
                        ActiveAndroid.endTransaction();
                        // Send a new event with results from network
                        searchLatestMeasurementsFromDB(location, city);
                    } else {
                        // Null result
                        // We may want to display a warning to user (e.g. Toast)
                        Log.e("[CovidAir] [MEASUREMENT] [REST]", "Response error : null body");
                    }
                }


                @Override
                public void onFailure(Call<MeasurementSearchResult> call, Throwable t) {
                    // Request has failed or is not at expected format
                    // We may want to display a warning to user (e.g. Toast)
                    Log.e("[CovidAir] [MEASUREMENT] [REST]", "Response error : " + t.getMessage());
                }
            });
        }, REFRESH_DELAY, TimeUnit.MILLISECONDS);
    }


    private void searchLatestMeasurementsFromDB(String location, String city) {
        String[] params = {"pm25", "pm10", "so2", "no2", "o3", "co", "bc"};
        List<Measurement> latestMeasurementsFromDB = new ArrayList<>();
        for (String parameter : params){
            List<Measurement> matchingLatest = new Select()
                    .from(Measurement.class)
                    .where("location LIKE '%" +location+"%' AND city LIKE'%"+ city + "%' AND parameter LIKE'%"+ parameter + "%'")
                    .orderBy("orderDate DESC")
                    .limit(1)
                    .execute();
            if (matchingLatest != null && !matchingLatest.isEmpty()){
                latestMeasurementsFromDB.add(matchingLatest.get(0));
            }
        }
        EventBusManager.BUS.post(new SearchMeasurementResultEvent(latestMeasurementsFromDB));
    }
}