package com.google.android.bootcamp.memegen;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by chungha on 12/15/14.
 */
public class AddressToGeoPointTranslator {
  public static Pair<Double, Double> run(Context context, String address) throws IOException {
    Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
    List<Address> result = geoCoder.getFromLocationName(address, 1);
    if (result.size() > 0) {
      double latitude = result.get(0).getLatitude();
      double longitude = result.get(0).getLongitude();
      return new Pair<Double, Double>(latitude, longitude);
    }
    return null;
  }
}
