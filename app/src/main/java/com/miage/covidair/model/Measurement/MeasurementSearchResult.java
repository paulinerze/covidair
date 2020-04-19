package com.miage.covidair.model.Measurement;

import com.google.gson.annotations.Expose;

import java.util.List;

public class MeasurementSearchResult {
    @Expose
    public List<Measurement> results;

    public MeasurementSearchResult(List<Measurement> results) {
        this.results = results;
    }
}
