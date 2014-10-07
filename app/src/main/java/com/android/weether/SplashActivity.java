package com.android.weether;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.android.weether.task.LoadWeatherTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends Activity {

    private static final String TAG = "SplashActivity";
    private String WEATHER_URL;
    private Context mContext = this;

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

                Log.d("TAG", "Weather URL = " + WEATHER_URL);

                mLoadWeatherTask = new LoadWeatherTask(mContext);
                mLoadWeatherTask.execute(WEATHER_URL);

                WeatherListModel.instance().address = findGeoCode(location);
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

}
