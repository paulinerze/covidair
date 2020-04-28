package com.miage.covidair.event;

import com.miage.covidair.model.Favorite;

import java.util.List;

public class SearchFavoriteResultEvent {

    private List<Favorite> favorites;

    public SearchFavoriteResultEvent(List<Favorite> favorites) {
        this.favorites = favorites;
    }

    public List<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
    }
}
