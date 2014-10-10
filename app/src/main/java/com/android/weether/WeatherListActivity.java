package com.android.weether;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.weether.task.LoadWeatherTask;
import com.android.weether.util.GeoCode;
import com.loopj.android.image.SmartImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class to display fetched weather content
 * in custom ListView.
 *
 * @author Muaz Rahman
 *
 */
public class WeatherListActivity extends ListActivity {
    private static final String TAG = "WeatherListActivity";
    private Context mContext = this;

    private String mCity;
    private String mState;
    private TextView mBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weatherlist);

        Log.d(TAG, "onCreate method - WeatherListActivity");

        SharedPreferences days = getSharedPreferences("DAYS", 0);
        WeatherListModel.instance().numDays = days.getInt("num_days", 2);

        mBanner = (TextView) findViewById(R.id.banner);
        mBanner.setText("CURRENT AND NEXT " + WeatherListModel.instance().numDays + " DAYS");

        List<WeatherModel> weatherModel = new ArrayList<WeatherModel>();
        for(int i = 0; i <= WeatherListModel.instance().numDays; i++){
            weatherModel.add(WeatherListModel.instance().weatherList.get(i));
        }

        WeatherListAdapter adapter =
                new WeatherListAdapter(this, weatherModel);

        setListAdapter(adapter);
    }

    private class WeatherListAdapter extends ArrayAdapter<WeatherModel> {
        private final List<WeatherModel> mWeatherModels;
        private final Context mContext;

        public WeatherListAdapter(Context context, List<WeatherModel> models) {
            super(context, R.layout.weather_row, models);
            mWeatherModels = models;
            mContext = context;

        }

        SharedPreferences temp = getSharedPreferences("TEMP", 0);
        String temp_type = temp.getString("temp_type", "Farenheit");

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View weatherRow = inflater.inflate(R.layout.weather_row, parent, false);

            WeatherModel weatherModel = mWeatherModels.get(position);

            SmartImageView imageView = (SmartImageView) weatherRow.findViewById(R.id.icon);
            TextView dayTextView = (TextView) weatherRow.findViewById(R.id.weekday);
            TextView tempHighTextView = (TextView) weatherRow.findViewById(R.id.tempHigh);
            TextView tempLowTextView = (TextView) weatherRow.findViewById(R.id.tempLow);
            TextView descriptionTextView = (TextView) weatherRow.findViewById(R.id.description);

            imageView.setImageUrl(weatherModel.getIconURL());
            dayTextView.setText(weatherModel.getWeekday());
            descriptionTextView.setText(weatherModel.getConditions());

            if(temp_type.equals("Farenheit")) {
                tempHighTextView.setText(String.valueOf(weatherModel.getTempHighF()) + "°F");
                tempLowTextView.setText(String.valueOf(weatherModel.getTempLowF()) + "°F");
            }
            else {
                tempHighTextView.setText(String.valueOf(weatherModel.getTempHighC()) + "°C");
                tempLowTextView.setText(String.valueOf(weatherModel.getTempLowC()) + "°C");
            }

            return weatherRow;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather_list_activity, menu);
        ActionBar ab = getActionBar();

        if(!WeatherListModel.instance().refreshSelect) {
            SharedPreferences location = getSharedPreferences("LOCATIONS", 0);
            WeatherListModel.instance().city = location.getString("city", WeatherListModel.instance().city);
            WeatherListModel.instance().state = location.getString("state", WeatherListModel.instance().state);
        }

        mCity = WeatherListModel.instance().city;
        mState = WeatherListModel.instance().state;
        ab.setTitle(mCity + ", " + mState);

        Drawable d = getResources().getDrawable(R.drawable.weather_background);
        ab.setBackgroundDrawable(d);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_place:
                Toast.makeText(mContext.getApplicationContext(), "Fetching weather for current location...", Toast.LENGTH_LONG).show();
                refreshFeed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshFeed(){
        LocationManager mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener mlocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                GeoCode geocode = new GeoCode(getApplicationContext(), Locale.getDefault());

                if(WeatherListModel.instance().city.equals(geocode.find(location).get(0).getLocality())){
                        Toast.makeText(mContext.getApplicationContext(), "Weather already set for current location", Toast.LENGTH_LONG).show();
                }
                else {
                    WeatherListModel.instance().city = geocode.find(location).get(0).getLocality();
                    WeatherListModel.instance().state = geocode.find(location).get(0).getAdminArea();
                    WeatherListModel.instance().refreshSelect = true;

                    String WEATHER_URL = "http://api.wunderground.com/api/cd73277d18704fa9/forecast10day/q/" +
                            String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()) + ".json";

                    Log.d("TAG", "Weather URL = " + WEATHER_URL);

                    LoadWeatherTask mLoadWeatherTask = new LoadWeatherTask(mContext, true);
                    mLoadWeatherTask.execute(WEATHER_URL);
                }
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // Quit if back is pressed
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
