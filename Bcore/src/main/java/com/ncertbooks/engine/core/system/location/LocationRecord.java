package com.ncertbooks.engine.core.system.location;


public class LocationRecord {
    public String packageName;
    public int userId;

    public LocationRecord(String packageName, int userId) {
        this.packageName = packageName;
        this.userId = userId;
    }
}
