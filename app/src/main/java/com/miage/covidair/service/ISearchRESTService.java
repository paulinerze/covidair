package com.miage.covidair.service;

import com.miage.covidair.model.City.CitySearchResult;
import com.miage.covidair.model.Location.LatestSearchResult;
import com.miage.covidair.model.Location.LocationSearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ISearchRESTService {
    @GET("cities?")
    Call<CitySearchResult> searchForCities(@Query("country") String country,
                                           @Query("limit") int limit);

    @GET("locations?")
    Call<LocationSearchResult> searchForLocations(@Query("country") String country,
                                                  @Query("city") String city,
                                                  @Query("limit") int limit);

    @GET("latest?")
    Call<LatestSearchResult> searchForLatest(@Query("country") String country,
                                             @Query("city") String city,
                                             @Query("location") String location);
}
