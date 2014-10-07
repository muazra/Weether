package com.android.weether.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Muaz on 10/7/14.
 */
public class GeoCode {

    private Geocoder mGeocoder;

    public GeoCode(Context context, Locale locale){
        mGeocoder = new Geocoder(context, locale);
    }

    public List<Address> find(Location location){
        try {
            List<Address> address = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (address.size() > 0) {
                return address;
            }
        }catch(IOException e){}

        return null;
    }
}
