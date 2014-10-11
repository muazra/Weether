package com.android.weether;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import com.android.weether.task.LoadWeatherTask;
import com.android.weether.util.GeocodeUtil;
import com.android.weether.util.NetworkUtil;

import java.util.Locale;

public class SplashActivity extends Activity {

    private static final String TAG = "SplashActivity";
    private TextView mSplashText;
    private Context mContext = this;
    private SharedPreferences mlocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(!NetworkUtil.isOnline(mContext)) {
            buildDialog().show();
        }
        else{
            mSplashText = (TextView) findViewById(R.id.splash_textView);
            mlocation = getSharedPreferences("LOCATIONS", 0);
            setLocation();
        }
    }

    protected void setLocation(){
        if (mlocation.getBoolean("locations_exist", false)) {
            mSplashText.setText(R.string.splash_savedlocation);
            serveSavedLocation();
        } else {
            mSplashText.setText(R.string.splash_currentlocation);
            serveCurrentLocation();
        }
    }

    protected void serveSavedLocation(){
        LoadWeatherTask LoadWeatherTask = new LoadWeatherTask(mContext, false);
        LoadWeatherTask.execute(mlocation.getString("weather_url", "null"));
    }

    protected void serveCurrentLocation(){
        LocationManager mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener mlocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                GeocodeUtil geocode = new GeocodeUtil(getApplicationContext(), Locale.getDefault());

                SharedPreferences.Editor editor = mlocation.edit();
                editor.putBoolean("locations_exist", false);
                editor.putString("city", geocode.find(location).get(0).getLocality());
                editor.putString("state", geocode.find(location).get(0).getAdminArea());
                editor.apply();

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

    private AlertDialog.Builder buildDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle("Internet Access Disabled");
        builder.setMessage("Please enable and try again.");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });
        builder.setIcon(R.drawable.ic_action_warning);
        return builder;
    }

}
