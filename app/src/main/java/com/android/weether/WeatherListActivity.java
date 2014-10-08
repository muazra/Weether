package com.android.weether;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;

import java.util.List;

/**
 * Class to display fetched weather content
 * in custom ListView.
 *
 * @author Muaz Rahman
 *
 */
public class WeatherListActivity extends ListActivity {
    private static final String TAG = "WeatherListActivity";
    private TextView mAddressView;
    private String mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weatherlist);

        WeatherListAdapter adapter =
                new WeatherListAdapter(this, WeatherListModel.instance().weatherList);

        mAddressView = (TextView) findViewById(R.id.address);
        mAddress = WeatherListModel.instance().address.get(0).getLocality() + "-" + WeatherListModel.instance().address.get(0).getAdminArea();
        mAddressView.setText(mAddress);

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
            TextView tempTextView = (TextView) weatherRow.findViewById(R.id.temp);
            TextView descriptionTextView = (TextView) weatherRow.findViewById(R.id.description);

            imageView.setImageUrl(weatherModel.getIconURL());
            dayTextView.setText(weatherModel.getWeekday());
            tempTextView.setText(String.valueOf(weatherModel.getTempHighF()));
            descriptionTextView.setText(weatherModel.getConditions());

            return weatherRow;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.weather_list_activity, menu);
        return true;
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
