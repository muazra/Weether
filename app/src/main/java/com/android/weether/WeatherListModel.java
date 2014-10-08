package com.android.weether;

import android.location.Address;

import java.util.List;

/**
 * Singleton class for weather list and weather address
 * @author Muaz Rahman
 */
public class WeatherListModel {
    public List<WeatherModel> weatherList;
    public List<Address> address;
    public int numDays;
    private WeatherListModel() {}

    static WeatherListModel obj = null;
    public static synchronized WeatherListModel instance() {
        if (obj == null) obj = new WeatherListModel();
        return obj;
    }
}
