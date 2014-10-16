package com.android.weether.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import com.android.weether.R;

/**
 * Class to determine whether or not
 * location services is enabled.
 *
 * @author Muaz Rahman
 *
 */
public class LocationEnabledUtil {

    public static void checkLocationEnabled(LocationManager lm, final Context mContext){
        if(!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_DARK);
            dialog.setTitle(mContext.getResources().getString(R.string.location_disabled));
            dialog.setMessage(mContext.getResources().getString(R.string.enable_location));
            dialog.setPositiveButton(mContext.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(myIntent);

                    System.exit(0);
                }
            });
            dialog.setNegativeButton(mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                }
            });
            dialog.show();
        }
    }
}
