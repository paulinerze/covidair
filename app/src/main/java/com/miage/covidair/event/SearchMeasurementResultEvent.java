package com.miage.covidair.event;

import com.miage.covidair.model.Measurement.Measurement;

import java.util.List;

public class SearchMeasurementResultEvent {
    private List<Measurement> measurements;

    public SearchMeasurementResultEvent(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public List<Measurement> getDetails() {
        return measurements;
    }
}

