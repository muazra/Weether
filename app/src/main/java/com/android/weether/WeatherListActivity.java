package com.android.weether;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
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

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weatherlist);

        WeatherListAdapter adapter =
                new WeatherListAdapter(this, WeatherListModel.instance().weatherList);

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
            TextView dayTextView = (TextView) weatherRow.findViewById(R.id.day);
            TextView tempTextView = (TextView) weatherRow.findViewById(R.id.temp);
            TextView descriptionTextView = (TextView) weatherRow.findViewById(R.id.description);

            imageView.setImageUrl(weatherModel.getIcon());
            dayTextView.setText(String.valueOf(weatherModel.getDay()));
            tempTextView.setText(String.valueOf(weatherModel.getTempHighF()));
            descriptionTextView.setText(weatherModel.getConditions());

            return weatherRow;
        }
    }


}
