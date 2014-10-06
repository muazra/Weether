package com.android.weether;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.android.weether.task.LoadWeatherTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends Activity {

    private static final String TAG = "SplashActivity";
    private String WEATHER_URL;

    LocationManager mlocationManager;
    LocationListener mlocationListener;
    LoadWeatherTask mLoadWeatherTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                WEATHER_URL = "http://api.wunderground.com/api/cd73277d18704fa9/forecast/q/" +
                        String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()) + ".json";

                //Log.d(TAG, "Weather URL = " + WEATHER_URL);

                AssetManager manager = getApplicationContext().getAssets();
                try{
                    InputStream is = manager.open("weather.json");
                    parseJSON(buildString(is));
                }catch(IOException e) {}

                //mLoadWeatherTask = new LoadWeatherTask();
                //mLoadWeatherTask.execute(WEATHER_URL);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            @Override
            public void onProviderEnabled(String s) {}
            @Override
            public void onProviderDisabled(String s) {}
        };

        mlocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mlocationListener, null);

    }

    protected List<Address> findGeoCode(Location location){
        Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> address = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (address.size() > 0) {
                return address;
            }
        }catch(IOException e){}

        return null;
    }

    //temporary functions
    private String buildString(InputStream is){

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String line;
        try{
            while((line = reader.readLine()) != null){
                builder.append(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return builder.toString();
    }

    private List<Weather> parseJSON(String jsonString){

        List<Weather> weatherList = new ArrayList<Weather>();

        try{
            JSONObject response = new JSONObject(jsonString);
            JSONArray array = response.getJSONObject("forecast").getJSONObject("simpleforecast").getJSONArray("forecastday");
            JSONObject jsonObject = array.getJSONObject(0);

            Log.d(TAG, jsonObject.getJSONObject("date").getString("weekday"));

        }catch(JSONException e){
            e.printStackTrace();
        }

        return null;
    }

}
