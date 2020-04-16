package com.miage.covidair.model.Location;

import com.activeandroid.Model;
import com.google.gson.annotations.Expose;

import java.util.List;

public class Latest extends Model {
    @Expose
    public List<Measurement> measurements;

}
