package com.miage.covidair.event;

import com.miage.covidair.model.Measurement.Measurement;

import java.util.List;

public class SearchDetailResultEvent {
    private List<Measurement> measurements;

    public SearchDetailResultEvent(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public List<Measurement> getDetails() {
        return measurements;
    }
}

