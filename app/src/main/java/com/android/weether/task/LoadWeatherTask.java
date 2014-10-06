package com.android.weether.task;

import android.os.AsyncTask;
import android.util.Log;

import com.android.weether.Weather;

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
 * class to fetch weather content. Uses Async task for separating
 * network call from main thread.
 *
 * @author Muaz Rahman
 *
 */
public class LoadWeatherTask extends AsyncTask<String, Integer, List<Weather>> {
    private static final String TAG = "LoadWeatherTask";

    @Override
    protected List<Weather> doInBackground(String... params){
        return fetchContent(params[0]);
    }

    /**
     * Method to fetch the Weather content
     *
     * @param URL
     *           :  String URL to make the HTTP request
     * @return List Containing Weather class objects
     */
    private List<Weather> fetchContent(String URL){
        Log.d(TAG, "fetchContent");

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

    /**
     * Method to convert the received input stream data to String
     *
     * @param is
     *          :  Input stream received as HTTP response
     * @return : Converted String
     */
    private String buildString(InputStream is){
        Log.d(TAG, "buildString");

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

    /**
     * Parse JSON string
     *
     * @param jsonString
     *            :  String received as response from URL
     * @return : List of type Article
     */
    private List<Weather> parseJSON(String jsonString){
        Log.d(TAG, "parseJSON");

        List<Weather> weatherList = new ArrayList<Weather>();

        try{
            JSONObject respJson = new JSONObject(jsonString);
            JSONArray jsonArray = respJson.getJSONObject("forecast").getJSONObject("simpleforecast").getJSONArray("forecastday");
            JSONObject jsonObject;

            for(int i = 0; i < jsonArray.length(); i++) {
                Weather weather = new Weather();
                jsonObject = jsonArray.getJSONObject(i);

                weather.setYear(jsonObject.getJSONObject("date").getInt("year"));
                weather.setDay(jsonObject.getJSONObject("date").getInt("day"));
                weather.setMonthname(jsonObject.getJSONObject("date").getString("monthname"));
                weather.setWeekday(jsonObject.getJSONObject("date").getString("weekday"));

                weather.setConditions(jsonObject.getString("conditions"));
                weather.setIcon(jsonObject.getString("icon"));

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
    protected void onPostExecute(List<Weather> result) {
       //do nothing
    }

}
