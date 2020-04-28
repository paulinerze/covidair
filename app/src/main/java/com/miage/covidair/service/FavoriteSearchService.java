package com.miage.covidair.service;

import com.activeandroid.query.Select;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchFavoriteResultEvent;
import com.miage.covidair.model.Favorite;

import java.util.List;

public class FavoriteSearchService {
    public static FavoriteSearchService INSTANCE = new FavoriteSearchService();

    public void searchFavorites(){
        List<Favorite> matchingFavoritesFromDB = new Select()
                .from(Favorite.class)
                .orderBy("location ASC")
                .execute();

        EventBusManager.BUS.post(new SearchFavoriteResultEvent(matchingFavoritesFromDB));

    }
}
