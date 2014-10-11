package com.android.weether.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Class to get address of user location
 * based on latitude and longitude coordinates
 *
 * @author Muaz Rahman
 *
 */
public class GeocodeUtil {

    private Geocoder mGeocoder;

    public GeocodeUtil(Context context, Locale locale){
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
