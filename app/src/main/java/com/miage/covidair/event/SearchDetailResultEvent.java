package com.miage.covidair.event;

import com.miage.covidair.model.Detail;

import java.util.List;

public class SearchDetailResultEvent {
    private List<Detail> details;

    public SearchDetailResultEvent(List<Detail> details) {
        this.details = details;
    }

    public List<Detail> getDetails() {
        return details;
    }
}

