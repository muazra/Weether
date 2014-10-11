package com.android.weether;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

import com.android.weether.task.GetLocationTask;
import com.android.weether.task.LoadWeatherTask;
import com.android.weether.util.NetworkUtil;

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
 * Class to display settings options.
 *
 * @author Muaz Rahman
 *
 */
public class SettingsActivity extends Activity {
    private static final String TAG = "SettingsActivity";
    private Context mContext = this;

    private ProgressBar mProgressBar;
    private EditText mEnterZipcode;

    private TextView mDefaultLocation;
    private TextView mDefaultDays;
    private TextView mDefaultTemp;

    private Button mCancel;
    private Button mSave;
    private Button mClearDefaults;

    private Spinner mSpinnerDays;
    private Spinner mSpinnerTemp;

    private String tempSelected;
    private int daysSelected;

    private SharedPreferences mLocation;
    private SharedPreferences mDays;
    private SharedPreferences mTemp;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_settings);
        mEnterZipcode = (EditText) findViewById(R.id.enter_zipcode);

        mDefaultLocation = (TextView) findViewById(R.id.current_default_location);
        mDefaultDays = (TextView) findViewById(R.id.current_default_days);
        mDefaultTemp = (TextView) findViewById(R.id.current_default_temp);

        mSpinnerDays = (Spinner) findViewById(R.id.spinner_days);
        mSpinnerTemp = (Spinner) findViewById(R.id.spinner_temp);

        mCancel = (Button) findViewById(R.id.cancel_settings);
        mSave = (Button) findViewById(R.id.save_settings);
        mClearDefaults = (Button) findViewById(R.id.clear_defaults_button);

        mLocation = getSharedPreferences("LOCATIONS", 0);
        mDays = getSharedPreferences("DAYS", 0);
        mTemp = getSharedPreferences("TEMP", 0);

        setupDefaults();
        setupSpinners();
        setupButtons();

    }

    private void setupButtons(){
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WeatherListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mClearDefaults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!NetworkUtil.isOnline(mContext)) {
                    Toast.makeText(mContext.getApplicationContext(), R.string.internet_disabled_clear,
                            Toast.LENGTH_LONG).show();
                }else {
                    buildClearDefaultsDialog().show();
                }
            }

        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if(!NetworkUtil.isOnline(mContext)) {
                    Toast.makeText(mContext.getApplicationContext(), R.string.internet_disabled_save,
                            Toast.LENGTH_LONG).show();
                }else {
                    AlertDialog dialog = buildSaveDialog().show();
                    TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                    textView.setTextSize(17);
                }
            }
        });
    }

    private void commitClearDefaultsChanges(){
        SharedPreferences.Editor editor = mDays.edit();
        editor.putInt("num_days", 3);
        mDefaultDays.setText("Default: 3");
        editor.apply();

        editor = mTemp.edit();
        editor.putString("temp_type", "Fahrenheit");
        mDefaultTemp.setText("Default: Fahrenheit");
        editor.apply();

        editor = mLocation.edit();
        editor.putBoolean("locations_exist", false);
        mDefaultLocation.setText("Default: Current Location");
        editor.apply();

        mEnterZipcode.setText("");
        new GetLocationTask(mContext).serveCurrentLocation();
    }

    private AlertDialog.Builder buildClearDefaultsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.confirmation);
        builder.setMessage(String.valueOf(R.string.confirmation_clear_defaults));
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {}
        });
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mProgressBar.setVisibility(View.VISIBLE);
                commitClearDefaultsChanges();
            }
        });
        builder.setIcon(R.drawable.ic_action_warning);

        return builder;
    }


    private void commitSaveChanges(){
        mProgressBar.setVisibility(View.VISIBLE);

        if(!(mEnterZipcode.getText().toString()).equals("")){
            ZipcodeLookup zipLookup = new ZipcodeLookup();
            zipLookup.execute(mEnterZipcode.getText().toString());
        }
        else {
            SharedPreferences.Editor editor = mDays.edit();
            editor.putInt("num_days", daysSelected);
            editor.apply();

            editor = mTemp.edit();
            editor.putString("temp_type", tempSelected);
            editor.apply();

            Intent intent = new Intent(mContext, WeatherListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }

    private AlertDialog.Builder buildSaveDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.confirmation_settings);
        builder.setMessage("Days Displayed = " + daysSelected + "\nTemperature Type = " + tempSelected +
                "\nWeather Location = " + getLocationMessage());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                    commitSaveChanges();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.ic_action_warning);
        return builder;
    }

    private String getLocationMessage(){
        if(mEnterZipcode.getText().toString().equals("")){
            if(mLocation.getBoolean("locations_exist", false))
                return mLocation.getString("city", "none") + ", " +
                        mLocation.getString("state", "none");
            else {
                return "Current Location";
            }
        }else
            return mEnterZipcode.getText().toString();
    }

    private void setupDefaults(){
        if(!mLocation.getBoolean("locations_exist", false))
            mDefaultLocation.setText("Default: Current Location");
        else
            mDefaultLocation.setText("Default: " + mLocation.getString("city", "none") + "," +
                    mLocation.getString("state", "none"));

        mDefaultDays.setText("Default: " + mDays.getInt("num_days", 3));
        mDefaultTemp.setText("Default: " + mTemp.getString("temp_type", "Fahrenheit"));
    }

    private void setupSpinners(){
        mSpinnerDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String day = (String) adapterView.getItemAtPosition(i);
                daysSelected = Integer.parseInt(day);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mSpinnerTemp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tempSelected = (String) adapterView.getItemAtPosition(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private class ZipcodeLookup extends AsyncTask<String, Void, String>{
        SharedPreferences.Editor editor;
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

                editor = mLocation.edit();
                editor.putBoolean("locations_exist", true);
                editor.putString("city", respJson.getString("city"));
                editor.putString("state", respJson.getString("state"));
                editor.apply();

                editor = mDays.edit();
                editor.putInt("num_days", daysSelected);
                editor.apply();

                editor = mTemp.edit();
                editor.putString("temp_type", tempSelected);
                editor.apply();

            }catch(JSONException e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String zipcode){
            String URL = "http://api.wunderground.com/api/cd73277d18704fa9/forecast10day/q/" + zipcode + ".json";
            editor = mLocation.edit();
            editor.putString("weather_url", URL);
            editor.apply();

            LoadWeatherTask loadWeatherTask = new LoadWeatherTask(mContext, false);
            loadWeatherTask.execute(URL);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
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
        View view = this.getCurrentFocus();
        if (view != null)
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mProgressBar.setVisibility(View.INVISIBLE);
    }

}
