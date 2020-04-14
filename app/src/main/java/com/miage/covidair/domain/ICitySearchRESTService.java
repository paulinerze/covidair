package com.miage.covidair.domain;

import com.miage.covidair.model.CitySearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ICitySearchRESTService {
    @GET("cities?")
    Call<CitySearchResult> searchForCities(@Query("coutry") String country, @Query("limit") int limit);
}
