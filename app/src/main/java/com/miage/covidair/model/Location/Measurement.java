package com.miage.covidair.model.Location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@Table(name = "Measurement")
public
class Measurement extends Model {
    @Expose
    @Column(name = "key", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String key;
    @Expose
    @Column(name = "parameter")
    public String parameter;
    @Expose
    @Column(name = "value")
    public String value;
    @Expose
    @Column(name = "unit")
    public String unit;


}
