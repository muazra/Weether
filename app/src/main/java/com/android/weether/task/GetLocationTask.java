package com.android.weether.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.android.weether.util.GeocodeUtil;

import java.util.Locale;

/**
 * Class to get location of user via
 * location manager.
 *
 * @author Muaz Rahman
 *
 */
public class GetLocationTask {

    private Context mContext;
    private SharedPreferences mLocation;

    public GetLocationTask(Context context){
        mContext = context;
        mLocation = context.getSharedPreferences("LOCATIONS", 0);
    }

    public void serveCurrentLocation(){
        LocationManager mlocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        LocationListener mlocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                GeocodeUtil geocode = new GeocodeUtil(mContext.getApplicationContext(), Locale.getDefault());

                Log.d("GetLocationTask", "Location found");

                SharedPreferences.Editor editor = mLocation.edit();
                editor.putBoolean("locations_exist", false);
                editor.putString("city", geocode.find(location).get(0).getLocality());
                editor.putString("state", geocode.find(location).get(0).getAdminArea());
                editor.commit();

                String WEATHER_URL = "http://api.wunderground.com/api/cd73277d18704fa9/forecast10day/q/" +
                        String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()) + ".json";

                LoadWeatherTask LoadWeatherTask = new LoadWeatherTask(mContext, false);
                LoadWeatherTask.execute(WEATHER_URL);
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
}
