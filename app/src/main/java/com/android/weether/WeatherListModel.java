package com.android.weether;

import java.util.List;

/**
 * Singleton class for list of WeatherModels.
 * @author Muaz Rahman
 */
public class WeatherListModel {
    public List<WeatherModel> weatherList;
    private WeatherListModel() {}

    static WeatherListModel obj = null;
    public static synchronized WeatherListModel instance() {
        if (obj == null) obj = new WeatherListModel();
        return obj;
    }
}
