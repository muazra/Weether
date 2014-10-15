package com.android.weether;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.android.weether.task.GetLocationTask;
import com.android.weether.task.LoadWeatherTask;
import com.android.weether.util.NetworkUtil;

/**
 * Splash Activity of Application
 *
 * @author Muaz Rahman
 *
 */
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
            new GetLocationTask(mContext).serveCurrentLocation();
        }
    }

    protected void serveSavedLocation(){
        LoadWeatherTask LoadWeatherTask = new LoadWeatherTask(mContext, false);
        LoadWeatherTask.execute(mlocation.getString("weather_url", "null"));
    }

    private AlertDialog.Builder buildDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.internet_disabled_title);
        builder.setMessage(R.string.enable_internet_message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });
        builder.setIcon(R.drawable.ic_action_warning);
        return builder;
    }

}
