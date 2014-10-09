package com.android.weether;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.weether.task.LoadWeatherTask;
import com.android.weether.util.GeoCode;

import java.util.Locale;

public class SplashActivity extends Activity {

    private static final String TAG = "SplashActivity";
    private Context mContext = this;
    private TextView mSplashText;

    LocationManager mlocationManager;
    LocationListener mlocationListener;
    LoadWeatherTask mLoadWeatherTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mSplashText = (TextView) findViewById(R.id.splash_textView);

        setNumDays();
        setLocation();
    }

    private void setNumDays(){
        SharedPreferences days = getSharedPreferences("DAYS", 0);
        WeatherListModel.instance().numDays = days.getInt("num_days", 2);
    }

    private void setLocation(){
        SharedPreferences location = getSharedPreferences("LOCATIONS", 0);
        boolean locations_exist = location.getBoolean("locations_exist", false);

        if (locations_exist) {
            mSplashText.setText(R.string.splash_savedlocation);
            serveSavedLocation(location);
        } else {
            mSplashText.setText(R.string.splash_currentlocation);
            serveCurrentLocation();
        }
    }

    private void serveSavedLocation(SharedPreferences location){
        mLoadWeatherTask = new LoadWeatherTask(mContext, false);
        WeatherListModel.instance().city = location.getString("city", "null");
        WeatherListModel.instance().state = location.getString("state", "null");
        String WEATHER_URL = location.getString("weather_url", "null");

        if(WEATHER_URL.equals("null"))
            serveCurrentLocation();
        else
            mLoadWeatherTask.execute(WEATHER_URL);
    }

    private void serveCurrentLocation(){
        mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String WEATHER_URL = "http://api.wunderground.com/api/cd73277d18704fa9/forecast10day/q/" +
                        String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()) + ".json";

                Log.d(TAG, "Weather URL = " + WEATHER_URL);

                mLoadWeatherTask = new LoadWeatherTask(mContext, true);
                mLoadWeatherTask.execute(WEATHER_URL);

                GeoCode geocode = new GeoCode(getApplicationContext(), Locale.getDefault());
                WeatherListModel.instance().city = geocode.find(location).get(0).getLocality();
                WeatherListModel.instance().state = geocode.find(location).get(0).getAdminArea();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            @Override
            public void onProviderEnabled(String s) {}
            @Override
            public void onProviderDisabled(String s) {
                Log.d(TAG, "Location services NOT enabled");
            }
        };

        mlocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mlocationListener, null);
    }

}
