package com.miage.covidair.event;

import com.miage.covidair.model.Location.Measurement;

import java.util.List;

public class SearchLatestMeasurementsResultEvent {
    private List<Measurement> measurements;

    public SearchLatestMeasurementsResultEvent(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }
}
