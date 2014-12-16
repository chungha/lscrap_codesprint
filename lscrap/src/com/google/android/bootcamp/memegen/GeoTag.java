package com.google.android.bootcamp.memegen;

public class GeoTag {
  public final String address;
  public final double latitude;
  public final double longitude;

  GeoTag(String addr, double lat, double lon) {
    address = addr;
    latitude = lat;
    longitude = lon;
  }
}
