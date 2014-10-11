package com.android.weether;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.weether.task.LoadWeatherTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Muaz on 10/8/14.
 */
public class SettingsActivity extends Activity {
    private static final String TAG = "SettingsActivity";
    private Button mCancel;
    private Button mSave;
    private Button mClear;
    private Spinner mSpinnerDays;
    private Spinner mSpinnerTemp;
    private EditText mEnterZipcode;
    private ProgressBar mProgressBar;
    private TextView mDefaultLocation;
    private TextView mDefaultDays;
    private TextView mDefaultTemp;

    private Context mContext = this;

    private String tempSelected;
    private String zipcodeEntered;
    private int daysSelected;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_settings);
        mEnterZipcode = (EditText) findViewById(R.id.enter_zipcode);

        mDefaultLocation = (TextView) findViewById(R.id.current_default_location);
        SharedPreferences location = getSharedPreferences("LOCATIONS", 0);
        if(location.getBoolean("locations_exist", false)){
            mDefaultLocation.setText("Default: " + location.getString("city", WeatherListModel.instance().city) + "," +
                    location.getString("state", WeatherListModel.instance().state));
        }
        else{
            mDefaultLocation.setText("Default: Current Location");
        }

        mDefaultDays = (TextView) findViewById(R.id.current_default_days);
        SharedPreferences days = getSharedPreferences("DAYS", 0);
        mDefaultDays.setText("Default: " + days.getInt("num_days", 2));

        mDefaultTemp = (TextView) findViewById(R.id.current_default_temp);
        SharedPreferences temp = getSharedPreferences("TEMP", 0);
        mDefaultTemp.setText("Default: " + temp.getString("temp_type", "Farenheit"));

        mSpinnerDays = (Spinner) findViewById(R.id.spinner_days);
        mSpinnerDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String day = (String) adapterView.getItemAtPosition(i);
                daysSelected = Integer.parseInt(day);
                Log.d(TAG, "Day selected = " + daysSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "NONE days selected");
            }
        });

        mSpinnerTemp = (Spinner) findViewById(R.id.spinner_temp);
        mSpinnerTemp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tempSelected = (String) adapterView.getItemAtPosition(i);
                Log.d(TAG, "Temp selected = " + tempSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "NONE temp selected");
            }
        });

        mCancel = (Button) findViewById(R.id.cancel_settings);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WeatherListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mClear = (Button) findViewById(R.id.clear_defaults_button);
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_DARK);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to clear all defaults to factory setting?");
                builder.setIcon(R.drawable.ic_action_warning);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                builder.show();
            }

        });

        mSave = (Button) findViewById(R.id.save_settings);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                hideKeyboard();

                SharedPreferences days = getSharedPreferences("DAYS", 0);
                if(days.getInt("num_days", 3) != daysSelected){
                    Log.d(TAG, "Editing days shared prefs..");
                    SharedPreferences.Editor editor = days.edit();
                    editor.putInt("num_days", daysSelected);
                    editor.commit();
                }

                SharedPreferences temp = getSharedPreferences("TEMP", 0);
                if(!temp.getString("temp_type", "Farenheit").equals(tempSelected)){
                    SharedPreferences.Editor editor = temp.edit();
                    editor.putString("temp_type", tempSelected);
                    editor.commit();
                }

                zipcodeEntered = mEnterZipcode.getText().toString();
                if(!zipcodeEntered.equals("")){
                    WeatherListModel.instance().refreshSelect = false;
                    ZipcodeLookup zipLookup = new ZipcodeLookup();
                    zipLookup.execute(zipcodeEntered);
                }
                else {
                    Intent intent = new Intent(mContext, WeatherListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    private class ZipcodeLookup extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params){
            String URL = "http://api.wunderground.com/api/cd73277d18704fa9/geolookup/q/" + params[0] + ".json";
            fetchContent(URL);
            return params[0];
        }

        private void fetchContent(String URL){
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(URL);
            try{
                HttpResponse response = httpClient.execute(httpget);
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                parseJSON(buildString(content));
            } catch(IOException e) {
                e.printStackTrace();
            }
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

        private void parseJSON(String jsonString){
            try{
                JSONObject respJson = new JSONObject(jsonString).getJSONObject("location");

                Log.d(TAG, respJson.toString());

                String city = respJson.getString("city");
                String state = respJson.getString("state");

                Log.d(TAG, "city = " + city);
                Log.d(TAG, "state = " + state);

                SharedPreferences location = getSharedPreferences("LOCATIONS", 0);
                SharedPreferences.Editor editor = location.edit();
                editor.putBoolean("locations_exist", true);
                editor.putString("city", city);
                editor.putString("state", state);
                editor.commit();

            }catch(JSONException e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String zipcode){
            String URL = "http://api.wunderground.com/api/cd73277d18704fa9/forecast10day/q/" + zipcode + ".json";
            Log.d(TAG, "URL via zipcode = " + URL);

            SharedPreferences location = getSharedPreferences("LOCATIONS", 0);
            SharedPreferences.Editor editor = location.edit();
            editor.putString("weather_url", URL);
            editor.commit();

            LoadWeatherTask loadWeatherTask = new LoadWeatherTask(mContext, false);
            loadWeatherTask.execute(URL);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case android.R.id.home:
                Intent intent = new Intent(this, WeatherListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mProgressBar.setVisibility(View.INVISIBLE);
    }

}
