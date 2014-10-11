package com.android.weether.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.android.weether.R;
import com.android.weether.WeatherListActivity;
import com.android.weether.WeatherListModel;
import com.android.weether.WeatherModel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to fetch weather content. Uses Async task for separating
 * network call from main thread.
 *
 * @author Muaz Rahman
 *
 */
public class LoadWeatherTask extends AsyncTask<String, Integer, List<WeatherModel>> {
    private static final String TAG = "LoadWeatherTask";
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private Boolean mShowDialog;

    public LoadWeatherTask(Context context, Boolean fetchCurrent){
        mContext = context;
        mShowDialog = fetchCurrent;
    }

    @Override
    protected void onPreExecute(){
        if(mShowDialog) {
            mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_DARK);
            mProgressDialog.setTitle(R.string.loading_dialog);
            mProgressDialog.setMessage(mContext.getString(R.string.waiting_dialog));
            mProgressDialog.show();
        }
    }

    @Override
    protected List<WeatherModel> doInBackground(String... params){
        return fetchContent(params[0]);
    }

    private List<WeatherModel> fetchContent(String URL){
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(URL);
        try{
            HttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            return parseJSON(buildString(content));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

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

    private List<WeatherModel> parseJSON(String jsonString){
        List<WeatherModel> weatherList = new ArrayList<WeatherModel>();

        try{
            JSONObject respJson = new JSONObject(jsonString);
            JSONArray jsonArray = respJson.getJSONObject("forecast").getJSONObject("simpleforecast").
                    getJSONArray("forecastday");
            JSONObject jsonObject;

            for(int i = 0; i < jsonArray.length(); i++) {
                WeatherModel weather = new WeatherModel();
                jsonObject = jsonArray.getJSONObject(i);

                weather.setYear(jsonObject.getJSONObject("date").getInt("year"));
                weather.setDay(jsonObject.getJSONObject("date").getInt("day"));
                weather.setMonthname(jsonObject.getJSONObject("date").getString("monthname"));
                weather.setWeekday(jsonObject.getJSONObject("date").getString("weekday_short"));

                weather.setConditions(jsonObject.getString("conditions"));
                weather.setIconURL(jsonObject.getString("icon_url"));

                weather.setTempHighF(jsonObject.getJSONObject("high").getInt("fahrenheit"));
                weather.setTempHighC(jsonObject.getJSONObject("high").getInt("celsius"));
                weather.setTempLowF(jsonObject.getJSONObject("low").getInt("fahrenheit"));
                weather.setTempLowC(jsonObject.getJSONObject("low").getInt("celsius"));

                weatherList.add(weather);
            }

        }catch(JSONException e){
            e.printStackTrace();
        }

        return weatherList;
    }

    @Override
    protected void onPostExecute(List<WeatherModel> result) {
        WeatherListModel.instance().weatherList = result;

        if(mShowDialog){
            Toast.makeText(mContext.getApplicationContext(), R.string.current_weather_displayed,
                    Toast.LENGTH_LONG).show();
            mProgressDialog.dismiss();
        }

        Intent i = new Intent(mContext, WeatherListActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mContext.startActivity(i);
    }

}
