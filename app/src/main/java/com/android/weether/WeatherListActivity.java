package com.android.weether;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.weether.task.LoadWeatherTask;
import com.android.weether.util.GeocodeUtil;
import com.android.weether.util.NetworkUtil;
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
    private SharedPreferences mDays;
    private SharedPreferences mTemp;
    private SharedPreferences mLocation;
    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weatherlist);

        mDays = getSharedPreferences("DAYS", 0);
        mTemp = getSharedPreferences("TEMP", 0);
        mLocation = getSharedPreferences("LOCATIONS", 0);

        TextView mBanner = (TextView) findViewById(R.id.banner);
        mBanner.setText("CURRENT AND NEXT " + (mDays.getInt("num_days", 3)-1) + " DAYS");

        List<WeatherModel> weatherModelTemp = new ArrayList<WeatherModel>();
        for(int i = 0; i < mDays.getInt("num_days", 3); i++)
            weatherModelTemp.add(WeatherListModel.instance().weatherList.get(i));

        WeatherListAdapter adapter = new WeatherListAdapter(this, weatherModelTemp);
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

            if((mTemp.getString("temp_type", "Farenheit")).equals("Farenheit")) {
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
        getActionBar().setTitle(mLocation.getString("city", "none") + ", " + mLocation.getString("state", "none"));
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.weather_background));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_place:
                if(!NetworkUtil.isOnline(mContext)) {
                    Toast.makeText(mContext.getApplicationContext(), "Internet Access Not Found", Toast.LENGTH_LONG).show();
                    return true;
                }
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
                GeocodeUtil geocode = new GeocodeUtil(getApplicationContext(), Locale.getDefault());

                if(mLocation.getString("city", "none").equals(geocode.find(location).get(0).getLocality())) {
                    Toast.makeText(mContext.getApplicationContext(), "Weather already set for current location", Toast.LENGTH_LONG).show();
                }
                else {
                    SharedPreferences.Editor editor = mLocation.edit();
                    editor.putString("cite", geocode.find(location).get(0).getLocality());
                    editor.putString("state", geocode.find(location).get(0).getAdminArea());
                    editor.apply();

                    String WEATHER_URL = "http://api.wunderground.com/api/cd73277d18704fa9/forecast10day/q/" +
                            String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()) + ".json";

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
            }
        };
        mlocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mlocationListener, null);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        // Quit if back is pressed
//        if (keyCode == KeyEvent.KEYCODE_BACK)
//        {
//            moveTaskToBack(true);
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

}
