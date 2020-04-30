package com.miage.covidair.service;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.model.Favorite;
import com.miage.covidair.model.Location.Latest;
import com.miage.covidair.model.Location.LatestSearchResult;
import com.miage.covidair.model.Location.Location;
import com.miage.covidair.model.Location.LocationSearchResult;
import com.miage.covidair.model.Measurement.Measurement;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private static final long REFRESH_DELAY = 650;
    public static LocationSearchService INSTANCE = new LocationSearchService();
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    public ISearchRESTService mISearchRESTService;
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
                        for (Location location : response.body().results) {
                            //TODO : test sur la date pour last update

                            if (city.equals(location.city)) {
                                location.location = location.location.toUpperCase();
                                location.city = location.city.toUpperCase();
                                location.coordinates.location = location.location; //TODO: INUTILE
                                location.longitude = location.coordinates.longitude;
                                location.latitude = location.coordinates.latitude;
                                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.FRENCH);
                                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
                                LocalDate date = LocalDate.parse(location.lastUpdated, inputFormatter);
                                String formattedDate = outputFormatter.format(date);
                                location.lastUpdated = formattedDate;
                                location.latestMeasurements = new HashMap<>();
                                location.save();

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
        List<Location> matchingLocationsFromDB = returnMatchingLocationFromDB(city);

        for (Location location : matchingLocationsFromDB) {
            if (mLastScheduleTask != null && !mLastScheduleTask.isDone()) {
                mLastScheduleTask.cancel(true);
            }
            mLastScheduleTask = mScheduler.schedule(() -> {
                // Step 1 : first run a local search from DB and post result
                searchLocationsFromDB(city);
                //searchLatestMeasurementsFromDB(location.city,location.location,"FR");

                // Step 2 : Call to the REST service
                mISearchRESTService.searchForLatest(location.latitude + "," + location.longitude, 750, 10000).enqueue(new Callback<LatestSearchResult>() {
                    @Override
                    public void onResponse(Call<LatestSearchResult> call, Response<LatestSearchResult> response) {
                        // Post an event so that listening activities can update their UI
                        if (response.body() != null && response.body().results != null) {
                            // Save all results in Database
                            ActiveAndroid.beginTransaction();
                            HashMap<String, Measurement> latestMeasurements = new HashMap<>();
                            for (Latest latest : response.body().results) {
                                for (Measurement measurement : latest.measurements) {
                                    latestMeasurements.put(measurement.parameter, measurement);
                                    measurement.location = location.location;
                                    measurement.key = location.location + measurement.parameter;
                                    measurement.save();
                                }
                            }
                            location.setLatestMeasurements(latestMeasurements);
                            location.save();

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
        List<Location> matchingLocationsFromDB = returnMatchingLocationFromDB(city);

        for (Location location : matchingLocationsFromDB) {
            searchLocationsFromDB(city);
            // Create AsyncTask
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    // Here we are in a new background thread
                    try {
                        final OkHttpClient okHttpClient = new OkHttpClient();
                        final Request request = new Request.Builder()
                                .url("https://www.infoclimat.fr/public-api/gfs/json?&_ll=" + location.latitude + "," + location.longitude +
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
                            location.pluie = prevision.getDouble("pluie");
                            location.vent = prevision.getJSONObject("vent_moyen").getDouble("10m");
                            JSONObject jsonTemperature = prevision.getJSONObject("temperature");
                            Double kelvin = jsonTemperature.getDouble("sol") - Double.valueOf(273.15);
                            BigDecimal celcius = new BigDecimal(kelvin).setScale(2, RoundingMode.HALF_EVEN);
                            location.sol = celcius.doubleValue();
                            if (location.latestMeasurements == null) {
                                HashMap<String, Measurement> latestMeasurements = new HashMap<>();
                                location.setLatestMeasurements(latestMeasurements);
                            }
                            location.save();
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

        int hour = Integer.parseInt(todaysDate.substring(11, 13));
        int newHour = 0;
        switch (hour) {
            case 0:
            case 1:
            case 2:
                newHour = 2;
                break;
            case 3:
            case 4:
            case 5:
                newHour = 5;
                break;
            case 6:
            case 7:
            case 8:
                newHour = 8;
                break;
            case 9:
            case 10:
            case 11:
                newHour = 11;
                break;
            case 12:
            case 13:
            case 14:
                newHour = 14;
                break;
            case 15:
            case 16:
            case 17:
                newHour = 17;
                break;
            case 18:
            case 19:
            case 20:
                newHour = 20;
                break;
            case 21:
            case 22:
            case 23:
                newHour = 23;
                break;
            default:
                newHour = 23;
                break;
        }


        if (newHour > 8) {
            todaysDate = todaysDate.substring(0, 10) + " " + newHour + ":00:00";
        } else {
            todaysDate = todaysDate.substring(0, 10) + " 0" + newHour + ":00:00";
        }
        return todaysDate;
    }

    private List<Location> returnMatchingLocationFromDB(String city) {
        List<Location> matchingLocationsFromDB = new Select()
                .from(Location.class)
                .where("city LIKE '%" + city + "%'")
                .orderBy("location ASC")
                .execute();
        return matchingLocationsFromDB;
    }

    private List<Location> returnMatchingOneLocationFromDB(String longitude, String latitude) {
        List<Location> matchingLocationsFromDB = new Select()
                .from(Location.class)
                .where("longitude LIKE '%" + longitude + "%' AND latitude LIKE '%" + latitude + "%'")
                .limit(1)
                .execute();
        return matchingLocationsFromDB;
    }

    private void searchLocationsFromDB(String city) {
        updateMeasurements(city);
        List<Location> matchingLocationsFromDB = new Select()
                .from(Location.class)
                .where("city LIKE '%" + city + "%'")
                .orderBy("location ASC")
                .execute();
        EventBusManager.BUS.post(new SearchLocationResultEvent(matchingLocationsFromDB));
    }

    public void searchLocationFromDB(String longitude, String latitude) {
        updateMeasurement(longitude, latitude);
        List<Location> matchingLocationsFromDB = new Select()
                .from(Location.class)
                .where("longitude LIKE '%" + longitude + "%' AND latitude LIKE '%" + latitude + "%'")
                .limit(1)
                .execute();
        EventBusManager.BUS.post(new SearchLocationResultEvent(matchingLocationsFromDB));
    }

    private void updateMeasurements(String city) {
        List<Location> locations = returnMatchingLocationFromDB(city);
        for (Location location : locations) {
            HashMap<String, Measurement> latestMeasurements = new HashMap<>();
            List<Measurement> measurements = LocationSearchService.INSTANCE.returnLatestMeasurements(location.location);
            for (Measurement measurement : measurements) {
                latestMeasurements.put(measurement.parameter, measurement);
            }
            ActiveAndroid.beginTransaction();
            location.latestMeasurements = latestMeasurements;
            location.save();
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
        }
    }


    private void updateMeasurement(String longitude, String latitude) {
        List<Location> locations = returnMatchingOneLocationFromDB(longitude, latitude);
        for (Location location : locations) {
            HashMap<String, Measurement> latestMeasurements = new HashMap<>();
            List<Measurement> measurements = LocationSearchService.INSTANCE.returnLatestMeasurements(location.location);
            for (Measurement measurement : measurements) {
                latestMeasurements.put(measurement.parameter, measurement);
            }
            ActiveAndroid.beginTransaction();
            location.latestMeasurements = latestMeasurements;
            location.save();
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
        }
    }

    public void addToFavorites(String latitude, String longitude) {
        List<Location> matchingLocationFromDB = new Select()
                .from(Location.class)
                .where("latitude LIKE '%" + latitude + "%' AND longitude LIKE '%" + longitude + "%'")
                .limit(1)
                .execute();

        if (matchingLocationFromDB != null && !matchingLocationFromDB.isEmpty()) {
            ActiveAndroid.beginTransaction();
            Favorite favorite = new Favorite();
            Location newLocation = matchingLocationFromDB.get(0);
            favorite.location = newLocation.location;
            favorite.city = newLocation.city;
            favorite.count = newLocation.count;
            favorite.lastUpdated = newLocation.lastUpdated;
            HashMap<String, Measurement> latestMeasurements = new HashMap<>();
            latestMeasurements = newLocation.latestMeasurements;
            favorite.latestMeasurements = latestMeasurements;
            favorite.latitude = newLocation.latitude;
            favorite.longitude = newLocation.longitude;
            favorite.sol = newLocation.sol;
            favorite.vent = newLocation.vent;
            favorite.pluie = newLocation.pluie;
            favorite.save();
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
            searchLocationFromDB(favorite.longitude, favorite.latitude);
        }

    }

    public void rmFromFavorites(String latitude, String longitude) {
        List<Favorite> matchingFavoriteFromDB = new Select()
                .from(Favorite.class)
                .where("latitude LIKE '%" + latitude + "%' AND longitude LIKE '%" + longitude + "%'")
                .limit(1)
                .execute();

        if (matchingFavoriteFromDB != null && !matchingFavoriteFromDB.isEmpty()) {
            ActiveAndroid.beginTransaction();
            Favorite newLocation = matchingFavoriteFromDB.get(0);
            new Delete()
                    .from(Favorite.class)
                    .where("location LIKE '%" + newLocation.location + "%'").
                    execute();
            //newLocation.delete();
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
            searchLocationFromDB(newLocation.longitude, newLocation.latitude);
        }


    }

    public boolean isFavorite(String location) {
        List<Favorite> matchingFavoriteFromDB = new Select()
                .from(Favorite.class)
                .where("location LIKE '%" + location + "%'")
                .limit(1)
                .execute();

        if (matchingFavoriteFromDB != null && !matchingFavoriteFromDB.isEmpty()) {
            Favorite favorite = matchingFavoriteFromDB.get(0);
            if (favorite != null) {
                return true;
            } else return false;
        } else return false;
    }

    public List<Measurement> returnLatestMeasurements(String location) {
        List<Measurement> matchingMeasurementsFromDB = new Select()
                .from(Measurement.class)
                .where("location LIKE '%" + location + "%'")
                .execute();

        return matchingMeasurementsFromDB;
    }

    public void bigSearch(String zone, String nom,
                          String minBC, String maxBC,
                          String minCO, String maxCO,
                          String minNO2, String maxNO2,
                          String minO3, String maxO3,
                          String minPM10, String maxPM10,
                          String minPM25, String maxPM25,
                          String minSO2, String maxSO2) {
        List<Location> matchingLocationsFromDB = new ArrayList<>();

        if (!nom.equals("")) {
            matchingLocationsFromDB = new Select()
                    .from(Location.class)
                    .where("city LIKE '%" + zone + "%' AND location LIKE '%" + nom + "%'")
                    .execute();
        } else if (!zone.equals("")){
            matchingLocationsFromDB = new Select()
                    .from(Location.class)
                    .where("city LIKE '%" + zone + "%'")
                    .execute();
        } else {
            matchingLocationsFromDB = new Select()
                    .from(Location.class)
                    .execute();
        }

        List<Integer> indicesASupprimer = new ArrayList<>();

        for (int j = 0; j < matchingLocationsFromDB.size(); j++) {
            Location location = matchingLocationsFromDB.get(j);
            List<Measurement> measurements = returnLatestMeasurements(location.location);

            if (!measurements.isEmpty()) {
                boolean measurementsAreMatching = true;
                boolean stop = false;
                while (measurementsAreMatching && !stop) {
                    for (int i = 0; i < measurements.size(); i++) {
                        Measurement measurement = measurements.get(i);
                        switch (measurement.parameter) {
                            case "bc":
                                if (!maxBC.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(maxBC)) <= 0;
                                }
                                stop = !measurementsAreMatching;
                                if (!minBC.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(minBC)) >= 0;
                                }
                                stop = !measurementsAreMatching;
                                break;
                            case "co":
                                if (!maxCO.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(maxCO)) <= 0;
                                }
                                stop = !measurementsAreMatching;
                                if (!minCO.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(minCO)) >= 0;
                                }
                                stop = !measurementsAreMatching;
                                break;
                            case "no2":
                                if (!maxNO2.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(maxNO2)) <= 0;
                                }
                                stop = !measurementsAreMatching;
                                if (!minNO2.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(minNO2)) >= 0;
                                }
                                stop = !measurementsAreMatching;
                                break;
                            case "o3":
                                if (!maxO3.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(maxO3)) <= 0;
                                }
                                stop = !measurementsAreMatching;
                                if (!minO3.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(minO3)) >= 0;
                                }
                                stop = !measurementsAreMatching;
                                break;
                            case "pm10":
                                if (!maxPM10.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(maxPM10)) <= 0;
                                }
                                stop = !measurementsAreMatching;
                                if (!minPM10.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(minPM10)) >= 0;
                                }
                                stop = !measurementsAreMatching;
                                break;
                            case "pm25":
                                if (!maxPM25.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(maxPM25)) <= 0;
                                }
                                stop = !measurementsAreMatching;
                                if (!minPM25.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(minPM25)) >= 0;
                                }
                                stop = !measurementsAreMatching;
                                break;
                            case "so2":
                                if (!maxSO2.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(maxSO2)) <= 0;
                                }
                                stop = !measurementsAreMatching;
                                if (!minSO2.equals("")) {
                                    measurementsAreMatching = Double.valueOf(measurement.value).compareTo(Double.valueOf(minSO2)) >= 0;
                                }
                                stop = !measurementsAreMatching;
                                break;
                        }
                    }
                    stop = true;
                }


                if (measurementsAreMatching) {
                    HashMap<String, Measurement> latestMeasurements = new HashMap<>();
                    for (Measurement measurement : measurements) {
                        latestMeasurements.put(measurement.parameter, measurement);
                    }
                    ActiveAndroid.beginTransaction();
                    location.latestMeasurements = latestMeasurements;
                    location.save();
                    ActiveAndroid.setTransactionSuccessful();
                    ActiveAndroid.endTransaction();
                } else {
                    indicesASupprimer.add(Integer.valueOf(j));
                }
            } else {
                indicesASupprimer.add(Integer.valueOf(j));
            }
        }

        for (int i = indicesASupprimer.size(); i > 0; i--) {
            matchingLocationsFromDB.remove(indicesASupprimer.get(i - 1).intValue());
        }
        EventBusManager.BUS.post(new SearchLocationResultEvent(matchingLocationsFromDB));
    }
}