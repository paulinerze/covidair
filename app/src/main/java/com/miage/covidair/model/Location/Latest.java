package com.miage.covidair.model.Location;

import com.activeandroid.Model;
import com.google.gson.annotations.Expose;
import com.miage.covidair.model.Measurement.Measurement;

import java.util.List;

public class Latest extends Model {
    @Expose
    public List<Measurement> measurements;

}
