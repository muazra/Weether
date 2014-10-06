package com.android.weether;

import android.location.Address;

import java.util.List;

/**
 * Singleton class for weather list and weather address
 * @author Muaz Rahman
 */
public class WeatherList {
    public List<Weather> cache;
    public List<Address> address;
    private WeatherList() {}

    static WeatherList obj = null;
    public static synchronized WeatherList instance() {
        if (obj == null) obj = new WeatherList();
        return obj;
    }
}
