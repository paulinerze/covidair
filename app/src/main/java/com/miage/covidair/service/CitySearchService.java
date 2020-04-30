package com.miage.covidair.service;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchCityResultEvent;
import com.miage.covidair.model.City.City;
import com.miage.covidair.model.City.CitySearchResult;
import com.miage.covidair.model.Location.Location;
import com.miage.covidair.model.Location.LocationSearchResult;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CitySearchService {
    private static final long REFRESH_DELAY = 650;
    public static CitySearchService INSTANCE = new CitySearchService();
    public ISearchRESTService mISearchRESTService;
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
        mISearchRESTService = retrofit.create(ISearchRESTService.class);
    }

    public void searchCities(String city) {
        // Cancel last scheduled network call (if any)
        if (mLastScheduleTask != null && !mLastScheduleTask.isDone()) {
            mLastScheduleTask.cancel(true);
        }

        if (city != null && !city.isEmpty()) {
            // Schedule a network call in REFRESH_DELAY ms
            mLastScheduleTask = mScheduler.schedule(() -> {
                // Step 1 : first run a local search from DB and post result
                searchCityFromDB(city);

                // Step 2 : Call to the REST service
                mISearchRESTService.searchForCities("FR", 10000).enqueue(new Callback<CitySearchResult>() {
                    @Override
                    public void onResponse(Call<CitySearchResult> call, retrofit2.Response<CitySearchResult> response) {
                        // Post an event so that listening activities can update their UI
                        if (response.body() != null && response.body().results != null) {
                            // Save all results in Database
                            ActiveAndroid.beginTransaction();
                            for (City city : response.body().results) {
                                city.name = city.name.toUpperCase();
                                city.save();
                            }
                            ActiveAndroid.setTransactionSuccessful();
                            ActiveAndroid.endTransaction();

                            // Send a new event with results from network
                            searchCityFromDB(city);
                            searchLocations(city);
                        } else {
                            // Null result
                            // We may want to display a warning to user (e.g. Toast)

                            Log.e("[CovidAir] [CITY] [REST]", "Response error : null body");
                        }
                    }

                    @Override
                    public void onFailure(Call<CitySearchResult> call, Throwable t) {
                        // Request has failed or is not at expected format
                        // We may want to display a warning to user (e.g. Toast)
                        Log.e("[CovidAir] [CITY] [REST]", "Response error : " + t.getMessage());
                    }
                });
            }, REFRESH_DELAY, TimeUnit.MILLISECONDS);
        } else {
            // Schedule a network call in REFRESH_DELAY ms
            mLastScheduleTask = mScheduler.schedule(() -> {
                // Step 1 : first run a local search from DB and post result
                searchCitiesFromDB();

                // Step 2 : Call to the REST service
                mISearchRESTService.searchForCities("FR", 10000).enqueue(new Callback<CitySearchResult>() {
                    @Override
                    public void onResponse(Call<CitySearchResult> call, retrofit2.Response<CitySearchResult> response) {
                        // Post an event so that listening activities can update their UI
                        if (response.body() != null && response.body().results != null) {
                            // Save all results in Database
                            ActiveAndroid.beginTransaction();
                            for (City city : response.body().results) {
                                city.name = city.name.toUpperCase();
                                city.save();
                            }
                            ActiveAndroid.setTransactionSuccessful();
                            ActiveAndroid.endTransaction();

                            // Send a new event with results from network
                            searchCitiesFromDB();
                            searchLocations(null);
                        } else {
                            // Null result
                            // We may want to display a warning to user (e.g. Toast)

                            Log.e("[CovidAir] [CITY] [REST]", "Response error : null body");
                        }
                    }

                    @Override
                    public void onFailure(Call<CitySearchResult> call, Throwable t) {
                        // Request has failed or is not at expected format
                        // We may want to display a warning to user (e.g. Toast)
                        Log.e("[CovidAir] [CITY] [REST]", "Response error : " + t.getMessage());
                    }
                });
            }, REFRESH_DELAY, TimeUnit.MILLISECONDS);
        }


    }

    private void searchLocations(String city) {
        if (city != null && !city.isEmpty()) {
            List<City> matchingCitiesFromDB = returnCityFromDB(city);
            for (City mcity : matchingCitiesFromDB) {
                // Schedule a network call in REFRESH_DELAY ms
                mLastScheduleTask = mScheduler.schedule(() -> {
                    searchCitiesFromDB();
                    // Step 2 : Call to the REST service
                    mISearchRESTService.searchForLocations("FR", city, 10).enqueue(new Callback<LocationSearchResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onResponse(Call<LocationSearchResult> call, retrofit2.Response<LocationSearchResult> response) {
                            // Post an event so that listening activities can update their UI
                            if (response.body() != null && response.body().results != null) {
                                // Save all results in Database
                                ActiveAndroid.beginTransaction();
                                Boolean saved = false;
                                for (Location location : response.body().results) {
                                    if (mcity.name.equals(location.city.toUpperCase()) && !saved) {
                                        mcity.longitude = location.coordinates.longitude;
                                        mcity.latitude = location.coordinates.latitude;
                                        mcity.save();
                                        saved = true;
                                    }
                                }
                                ActiveAndroid.setTransactionSuccessful();
                                ActiveAndroid.endTransaction();
                                searchCityFromDB(city);
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
        } else {
            List<City> matchingCitiesFromDB = returnCitiesFromDB();
            for (City mcity : matchingCitiesFromDB) {
                // Schedule a network call in REFRESH_DELAY ms
                mLastScheduleTask = mScheduler.schedule(() -> {
                    searchCitiesFromDB();
                    // Step 2 : Call to the REST service
                    mISearchRESTService.searchForLocations("FR", mcity.name, 10).enqueue(new Callback<LocationSearchResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onResponse(Call<LocationSearchResult> call, retrofit2.Response<LocationSearchResult> response) {
                            // Post an event so that listening activities can update their UI
                            if (response.body() != null && response.body().results != null) {
                                // Save all results in Database
                                ActiveAndroid.beginTransaction();
                                Boolean saved = false;
                                for (Location location : response.body().results) {
                                    if (mcity.name.equals(location.city.toUpperCase()) && !saved) {
                                        mcity.longitude = location.coordinates.longitude;
                                        mcity.latitude = location.coordinates.latitude;
                                        mcity.save();
                                        saved = true;
                                    }
                                }
                                ActiveAndroid.setTransactionSuccessful();
                                ActiveAndroid.endTransaction();
                                searchCitiesFromDB();
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
        }


    }

    private void searchCitiesFromDB() {
        List<City> matchingCitiesFromDB = new Select().from(City.class).orderBy("name").execute();
        EventBusManager.BUS.post(new SearchCityResultEvent(matchingCitiesFromDB));
    }

    private void searchCityFromDB(String city) {
        List<City> matchingCitiesFromDB = new Select().from(City.class).where("name LIKE '%" + city + "%'").orderBy("name").execute();
        EventBusManager.BUS.post(new SearchCityResultEvent(matchingCitiesFromDB));
    }

    private List<City> returnCitiesFromDB() {
        List<City> matchingCitiesFromDB = new Select().from(City.class).orderBy("name").execute();
        return matchingCitiesFromDB;
    }

    private List<City> returnCityFromDB(String city) {
        List<City> matchingCitiesFromDB = new Select().from(City.class).where("name LIKE '%" + city + "%'").orderBy("name").execute();
        return matchingCitiesFromDB;
    }

    public boolean isCity(String city) {
        List<City> cities = returnCityFromDB(city);
        if (cities != null && !cities.isEmpty()) {
            return true;
        } else return false;
    }
}
