package com.miage.covidair.service;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchFavoriteResultEvent;
import com.miage.covidair.model.Favorite;
import com.miage.covidair.model.Measurement.Measurement;

import java.util.HashMap;
import java.util.List;

public class FavoriteSearchService {
    public static FavoriteSearchService INSTANCE = new FavoriteSearchService();

    public void searchFavorites(){
        List<Favorite> matchingFavoritesFromDB = new Select()
                .from(Favorite.class)
                .orderBy("location ASC")
                .execute();

        for (Favorite favorite : matchingFavoritesFromDB){
            HashMap<String, Measurement> latestMeasurements = new HashMap<>();
            List<Measurement> measurements = LocationSearchService.INSTANCE.returnLatestMeasurements(favorite.location);
            for (Measurement measurement : measurements){
                latestMeasurements.put(measurement.parameter,measurement);
            }
            ActiveAndroid.beginTransaction();
            favorite.latestMeasurements = latestMeasurements;
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
        }

        List<Favorite> matchingUpdatedFavoritesFromDB = new Select()
                .from(Favorite.class)
                .orderBy("location ASC")
                .execute();


        EventBusManager.BUS.post(new SearchFavoriteResultEvent(matchingUpdatedFavoritesFromDB));

    }
}
