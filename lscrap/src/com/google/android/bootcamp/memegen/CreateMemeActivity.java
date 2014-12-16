package com.google.android.bootcamp.memegen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Executors;

import com.google.android.bootcamp.memegen.Store.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity for creating a meme.
 */
public class CreateMemeActivity extends Activity {

  private static final String TAG = CreateMemeActivity.class.getCanonicalName();
  private Store store;
  private MapFragment mapFragment;
  private ProgressDialog progress;
  
  private void addMarker(Place p, GoogleMap map) {
	map.addMarker(new MarkerOptions()
      .position(new LatLng(p.latitude, p.longitude))
      .title(p.url).snippet(p.address));
	map.setOnMarkerClickListener(new OnMarkerClickListener() {
		@Override
		public boolean onMarkerClick(final Marker marker) {
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(marker.getTitle()));
					startActivity(i);
				}
			}, 1000);
			return false;
		}		
	});
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    progress = ProgressDialog.show(this, "",
    	    "Loading....", true);
   
    store = new Store(this.getApplicationContext().getSharedPreferences("places_store", 0));
  
    if (getIntent() != null) {
      if (getIntent().getAction().equals(Intent.ACTION_SEND)) {
        final String message = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        Log.d(TAG, "intent.getAction().equals(Intent.ACTION_SEND) - " + message);

        Executors.newSingleThreadExecutor().submit(new Runnable() {
          @Override
          public void run() {
            Log.d(TAG, "Extraction!");

            TagContentExtractor e = new TagContentExtractor();
            KoreaAddressExtractor ke = new KoreaAddressExtractor(
            		getResources().getStringArray(R.array.korean_first_layer),
            		getResources().getStringArray(R.array.korean_second_layer));
            Place last = null;
            try {
              List<String> contents = e.extract(message);
              for (String c : contents) {
                List<String> addresses = ke.extract(c);
                for (String a : addresses) {
                  if (a.length() > 30) {
                	  continue;
                  }
                  Log.d(TAG, "+++ " + a);
                  Pair<Double, Double> p = AddressToGeoPointTranslator.run(CreateMemeActivity.this, a);
                  if (p != null) {
                    Log.d(TAG, "GeoTag OK - " + String.format("%s - %f / %f", a, p.first, p.second));

                    last = store.add(message, a, p.first, p.second);
                  } else {
                	Log.d(TAG, "GeoTag Fail!");
                  }
                  Log.d(TAG, "OK");
                }
              }
            } catch (IOException e1) {
              e1.printStackTrace();
            }

            // Attempts to extract geotagged location information. This works for
            // Google blogspot articles tagged with location when written.
            if (last == null) {
              String html = download(message);
              List<GeoTag> tags = GeoTagExtractor.getGeoTagsFromHtml(html);
              for (GeoTag tag : tags) {
                last = store.add(message, tag.address, tag.latitude, tag.longitude);
              }
            }

            Log.d(TAG, "refresh markers");
            final Place focusPlace = last;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
              @Override
              public void run() {
                  setContentView(R.layout.activity_create_meme);
                  mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
                  refreshMarkersAsync(focusPlace);

                  if (focusPlace == null) {
                      AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(CreateMemeActivity.this);
                      dlgAlert.setMessage("Geo-point hasn't been extracted.");
                      dlgAlert.setTitle("T_T");
                      dlgAlert.setPositiveButton("OK", null);
                      dlgAlert.setCancelable(true);
                      dlgAlert.create().show();
                  }
              }
            });
          }
        });
      }
    } else {
    	setContentView(R.layout.activity_create_meme);  
        
        mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
    	refreshMarkersAsync(null);
    }
  }
  
  private void moveCamera(Place p, GoogleMap map) {
	  map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(p.latitude, p.longitude), 15));
	  map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
  }
  
  private void refreshMarkers(final Place lastUpdate, GoogleMap map) {
	map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	Log.d(TAG, "add markers! : " + store.getPlaceSet().size());
	Place last = null;
	for (Place p : store.getPlaceSet()) {
		addMarker(p, map);
		last = p;
	}
	if (lastUpdate == null && last != null) {
		moveCamera(last, map);
	} else if (lastUpdate != null) {
		moveCamera(lastUpdate, map);
	}
  }
  
  private void refreshMarkersAsync(final Place lastUpdate) {
	  mapFragment.getMapAsync(new OnMapReadyCallback() {
  		@Override
  		public void onMapReady(GoogleMap map) {
  			progress.dismiss();
  			refreshMarkers(lastUpdate, map);
  		}
      });
  }

  public static String download(String url) {
      HttpClient client = new DefaultHttpClient();
      HttpGet request = new HttpGet(url);
      String html = "";
      try {
          HttpResponse response = client.execute(request);
          InputStream in;
          in = response.getEntity().getContent();
          BufferedReader reader = new BufferedReader(
                  new InputStreamReader(in));
          StringBuilder str = new StringBuilder();
          String line = null;
          while ((line = reader.readLine()) != null) {
              str.append(line);
          }
          in.close();
          html = str.toString();
      } catch (IllegalStateException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
      return html;
  }
}
