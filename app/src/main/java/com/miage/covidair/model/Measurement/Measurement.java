package com.miage.covidair.model.Measurement;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.util.Date;

@Table(name = "Measurement")
public class Measurement extends Model {
    @Expose
    @Column(name = "location", index = true, unique = false, onUniqueConflict = Column.ConflictAction.IGNORE)
    public String location;
    @Expose
    @Column(name = "parameter")
    public String parameter;
    @Expose
    @Column(name = "value")
    public String value;
    @Expose
    @Column(name = "unit")
    public String unit;
    @Expose
    @Column(name = "date")
    public Date utcDate;
    @Expose
    public MeasurementDate date;


}
