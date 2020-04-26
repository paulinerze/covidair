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
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.model.Location.Latest;
import com.miage.covidair.model.Location.LatestSearchResult;
import com.miage.covidair.model.Location.Loca;
import com.miage.covidair.model.Location.LocationSearchResult;
import com.miage.covidair.model.Measurement.Measurement;
import com.miage.covidair.model.Weather.Weather;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
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
    private static DecimalFormat df2 = new DecimalFormat("#.##");

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

        Retrofit retrofitWeather = new Retrofit.Builder()
                // Using OkHttp as HTTP Client
                .client(new OkHttpClient())
                // Having the following as server URL
                .baseUrl("https://www.infoclimat.fr/public-api/gfs/")
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
    public void searchLocationsWOotherCalls(String city) {
        // Cancel last scheduled network call (if any)
        if (mLastScheduleTask != null && !mLastScheduleTask.isDone()) {
            mLastScheduleTask.cancel(true);
        }
        // Schedule a network call in REFRESH_DELAY ms
        mLastScheduleTask = mScheduler.schedule(() -> {
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
                    } else {
                        // Null result
                        // We may want to display a warning to user (e.g. Toast)
                        Log.e("[CovidAir] [LOCATION2] [REST]", "Response error : null body");
                    }
                }


                @Override
                public void onFailure(Call<LocationSearchResult> call, Throwable t) {
                    // Request has failed or is not at expected format
                    // We may want to display a warning to user (e.g. Toast)
                    Log.e("[CovidAir] [LOCATION2] [REST]", "Response error : " + t.getMessage());
                }
            });
        }, REFRESH_DELAY, TimeUnit.MILLISECONDS);

    }

    public void searchLatestMeasurements(String city) {
        List<Loca> matchingLocationsFromDB = returnMatchingLocationFromDB(city);

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
                            searchWeather(city);
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

    public void searchWeather(String city) {
        List<Loca> matchingLocationsFromDB = returnMatchingLocationFromDB(city);

        for (Loca loca : matchingLocationsFromDB){
            searchLocationsFromDB(city);
            // Create AsyncTask
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    // Here we are in a new background thread
                    try {
                        final OkHttpClient okHttpClient = new OkHttpClient();
                        final Request request = new Request.Builder()
                                .url("https://www.infoclimat.fr/public-api/gfs/json?&_ll=" + loca.latitude + "," + loca.longitude +
                                        "&_auth=UkgCFVUrVnQFKFBnAXcLIgdvAjcNe1RzBHgEZwpvUi9TOFU0AmJSNFU7USxSf" +
                                        "QE3VXgEZwE6CDhWPQB4Xy1SM1I4Am5VPlYxBWpQNQEuCyAHKQJjDS1UcwRmBGQKYVIvU" +
                                        "zFVNQJpUi5VOFEyUmABK1VnBGUBPwgvVioAZl83UjlSNgJiVTZWNwVqUDABNwsgBysCZ" +
                                        "w1nVGUEMwQ3Cm5SMFMyVTACNVI5VWlRYlJlAStVZARmATcIM1YxAG5fMVI3Ui4CeVVPV" +
                                        "kcFd1ByAXMLagdyAn8NZ1QyBDM%3D&_c=a8c2984da207697b07c0c8ec4037473f")
                                .build();
                        okhttp3.Response response = okHttpClient.newCall(request).execute();

                        if (response != null && response.body() != null) {
                            ActiveAndroid.beginTransaction();
                            JSONObject jsonResult = new JSONObject(response.body().string());
                            String todaysDate = getTodaysDate();
                            JSONObject prevision = jsonResult.getJSONObject(todaysDate);
                            loca.pluie = prevision.getDouble("pluie");
                            loca.vent = prevision.getJSONObject("vent_moyen").getDouble("10m");
                            JSONObject jsonTemperature = prevision.getJSONObject("temperature");
                            Double kelvin = jsonTemperature.getDouble("sol") - Double.valueOf(273.15);
                            BigDecimal celcius = new BigDecimal(kelvin).setScale(2, RoundingMode.HALF_EVEN);
                            loca.sol = celcius.doubleValue();
                            if (loca.latestMeasurements == null){
                                HashMap<String,Measurement> latestMeasurements = new HashMap<>();
                                loca.setLatestMeasurements(latestMeasurements);
                            }
                            loca.save();
                            ActiveAndroid.setTransactionSuccessful();
                            ActiveAndroid.endTransaction();
                            searchLocationsFromDB(city);
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

    }



    @NotNull
    private String getTodaysDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
        Date date = new Date();
        String todaysDate = dateFormat.format(date);

        int hour = Integer.parseInt(todaysDate.substring(11,13));
        int newHour = 0;
        switch (hour){
            case 0:
            case 1:
            case 2: newHour = 2;
                break;
            case 3:
            case 4:
            case 5: newHour = 5;
                break;
            case 6:
            case 7:
            case 8: newHour = 8;
                break;
            case 9:
            case 10:
            case 11: newHour = 11;
                break;
            case 12:
            case 13:
            case 14: newHour = 14;
                break;
            case 15:
            case 16:
            case 17: newHour = 17;
                break;
            case 18:
            case 19:
            case 20: newHour = 20;
                break;
            case 21:
            case 22:
            case 23: newHour = 23;
                break;
            default: newHour = 23;
                break;
        }


        if (newHour > 8) {
            todaysDate = todaysDate.substring(0,10) + " " + newHour + ":00:00";
        } else {
            todaysDate = todaysDate.substring(0,10) + " 0" + newHour + ":00:00";
        }
        return todaysDate;
    }

    private List<Loca> returnMatchingLocationFromDB(String city){
        List<Loca> matchingLocationsFromDB = new Select()
                .from(Loca.class)
                .where("city LIKE '%" + city + "%'")
                .orderBy("location")
                .execute();
        return matchingLocationsFromDB;
    }
    private void searchLocationsFromDB(String city) {
        List<Loca> matchingLocationsFromDB = new Select()
                .from(Loca.class)
                .where("city LIKE '%" + city + "%'")
                .orderBy("location")
                .execute();
        EventBusManager.BUS.post(new SearchLocationResultEvent(matchingLocationsFromDB));
    }

    public void searchLocationFromDB(String city, String location) {
        List<Loca> matchingLocationsFromDB = new Select()
                .from(Loca.class)
                .where("city LIKE '%" + city + "%' AND location LIKE '%" + location + "%'")
                .limit(1)
                .execute();
        EventBusManager.BUS.post(new SearchLocationResultEvent(matchingLocationsFromDB));
    }

    public Loca returnFirstLocationFromDB(String city){
        List<Loca> matchingLocationFromDB = new Select()
                .from(Loca.class)
                .where("city LIKE '%" + city + "%'")
                .limit(1)
                .execute();
        if (matchingLocationFromDB != null && !matchingLocationFromDB.isEmpty()) {
            return matchingLocationFromDB.get(0);
        } else return null;
    }
}