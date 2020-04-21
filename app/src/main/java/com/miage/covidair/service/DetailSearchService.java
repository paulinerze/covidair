package com.miage.covidair.service;

import android.os.AsyncTask;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailSearchService {
    public static DetailSearchService INSTANCE = new DetailSearchService();
    public ISearchRESTService mISearchRESTService;
    private static final long REFRESH_DELAY = 650;
    private ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mLastScheduleTask;

    private DetailSearchService() {
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

    public void searchWeather(final String search) {
        // Create AsyncTask
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                // Here we are in a new background thread
                try {
                    final OkHttpClient okHttpClient = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url("https://www.infoclimat.fr/public-api/gfs/json?&_ll=" + search +
                                    "&_auth=UkgCFVUrVnQFKFBnAXcLIgdvAjcNe1RzBHgEZwpvUi9TOFU0AmJSNFU7USxSf" +
                                    "QE3VXgEZwE6CDhWPQB4Xy1SM1I4Am5VPlYxBWpQNQEuCyAHKQJjDS1UcwRmBGQKYVIvU" +
                                    "zFVNQJpUi5VOFEyUmABK1VnBGUBPwgvVioAZl83UjlSNgJiVTZWNwVqUDABNwsgBysCZ" +
                                    "w1nVGUEMwQ3Cm5SMFMyVTACNVI5VWlRYlJlAStVZARmATcIM1YxAG5fMVI3Ui4CeVVPV" +
                                    "kcFd1ByAXMLagdyAn8NZ1QyBDM%3D&_c=a8c2984da207697b07c0c8ec4037473f")
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    if (response != null && response.body() != null) {
                        JSONObject jsonResult = new JSONObject(response.body().string());
                        String todaysDate = getTodaysDate();
                        JSONArray jsonWeather = jsonResult.getJSONArray(todaysDate);
                        for (int i = 0; i < jsonWeather.length(); i++) {
                            JSONObject jsonTemperature = jsonWeather.getJSONObject(i);
                            Double twoM = jsonTemperature.getJSONObject("temperature").getDouble("sol");
                            Double sol = jsonTemperature.getJSONObject("temperature").getDouble("sol");
                            Log.d("RECEIVED temperature", String.valueOf(sol));
                        }
                    }
                } catch (IOException e) {
                    Log.e("[CovidAir] [WEATHER] [NETWORK] ", e.getMessage());
                } catch (JSONException e) {
                    Log.e("[CovidAir] [WEATHER] [JSON] ", e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    @NotNull
    private String getTodaysDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
        Date date = new Date();
        String todaysDate = dateFormat.format(date);
        int hour = Integer.parseInt(todaysDate.substring(11,12));
        int[] numbers = {2,5,8,11,14,17,20,23};
        List<Integer> list = Arrays.stream(numbers).boxed().collect(Collectors.toList());
        int c = list.stream()
                .min(Comparator.comparingInt(i -> Math.abs(i - hour)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
        if (c > 8) {
            todaysDate = todaysDate.substring(0,10) + " " + c + ":00:00";
        } else {
            todaysDate = todaysDate.substring(0,10) + " 0" + c + ":00:00";
        }
        return todaysDate;
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