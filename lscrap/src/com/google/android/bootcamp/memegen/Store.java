package com.google.android.bootcamp.memegen;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by chungha on 12/15/14.
 */
public class Store {
  private static final String TAG = Store.class.getCanonicalName();
  private SharedPreferences sp;
  public Store(SharedPreferences sp) {
    this.sp = sp;

    load();
  }

  public static class Place {
    public String url;
    public String address;
    public double latitude;
    public double longitude;

    static Place of(String url, String address, double latitude, double longitude) {
      Place p = new Place();
      p.url = url;
      p.address = address;
      p.latitude = latitude;
      p.longitude = longitude;
      return p;
    }

    static Place of(JSONObject json) {
      try {
        return Place.of(json.getString("url"),
            json.getString("address"),
            json.getDouble("latitude"),
            json.getDouble("longitude"));
      } catch (JSONException e) {
        e.printStackTrace();
      }
      return null;
    }

    public JSONObject toJsonObject() {
      JSONObject json = new JSONObject();
      try {
        json.put("url", url);
        json.put("address", address);
        json.put("latitude", latitude);
        json.put("longitude", longitude);
        return json;
      } catch (JSONException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Place) {
        Place p = (Place)o;
        return p.url.equals(this.url) || p.address.equals(this.address);
      }
      return false;
    }
  }

  private CopyOnWriteArraySet<Place> placeSet = new CopyOnWriteArraySet<Place>();

  public void add(String url, String address, double latitude, double longitude) {
    placeSet.add(Place.of(url, address, latitude, longitude));

    save();
  }

  private void load() {
    String jsonString = sp.getString("places", null);
    if (jsonString != null) {
      try {
        JSONObject json = new JSONObject(jsonString);
        JSONArray array = json.getJSONArray("places");
        for (int i = 0; i < array.length(); i++) {
          placeSet.add(Place.of((JSONObject) array.get(i)));
        }
        Log.d(TAG, "Load - " + jsonString);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  private void save() {
    JSONObject json = new JSONObject();
    JSONArray array = new JSONArray();
    for (Place p : placeSet) {
      array.put(p.toJsonObject());
    }
    try {
      json.put("places", array);
      String r = json.toString();
      sp.edit().putString("places", r).apply();

      Log.d(TAG, "Save - " + r);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}