package com.android.weether;

import java.util.List;

/**
 * Singleton class for weather list and weather address
 * @author Muaz Rahman
 */
public class WeatherListModel {
    public List<WeatherModel> weatherList;
    public int numDays;
    public String city;
    public String state;
    public boolean refreshSelect;

    private WeatherListModel() {
        refreshSelect = false;
    }

    static WeatherListModel obj = null;
    public static synchronized WeatherListModel instance() {
        if (obj == null) obj = new WeatherListModel();
        return obj;
    }
}
