package com.google.android.bootcamp.memegen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import com.google.android.bootcamp.memegen.Store.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity for creating a meme.
 */
public class CreateMemeActivity extends Activity {

  private static final String TAG = CreateMemeActivity.class.getCanonicalName();
  private Store store;
  private MapFragment mapFragment;
  
  private void addMarker(Place p, GoogleMap map) {
	map.addMarker(new MarkerOptions()
      .position(new LatLng(p.latitude, p.longitude))
      .title(p.url).snippet(p.address));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
   
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
            try {
              List<String> contents = e.extract(message);
              Place last = null;
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
              Log.d(TAG, "refresh markers");
              final Place focusPlace = last;
              new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					setContentView(R.layout.activity_create_meme);  
				    mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));   
				    refreshMarkersAsync(focusPlace);
				} 
              });
            } catch (IOException e1) {
              e1.printStackTrace();
            }
          }
        });

      }
    } else {
    	setContentView(R.layout.activity_create_meme);  
        
        mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
    	refreshMarkersAsync(null);
    }
  }
  
  private void moveCamara(Place p, GoogleMap map) {
	  map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(p.latitude, p.longitude), 15));
	  map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
  }
  
  private void refreshMarkers(final Place lastUpdate, GoogleMap map) {
	map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	Log.d(TAG, "add markers! : " + store.getPlaceSet().size());
	Place last = null;
	for (Place p : store.getPlaceSet()) {
		addMarker(p, map);
		last = p;
	}
	if (lastUpdate == null && last != null) {
		moveCamara(last, map);
	} else if (lastUpdate != null) {
		moveCamara(lastUpdate, map);
	}
  }
  
  private void refreshMarkersAsync(final Place lastUpdate) {
	  mapFragment.getMapAsync(new OnMapReadyCallback() {
  		@Override
  		public void onMapReady(GoogleMap map) {
  			refreshMarkers(lastUpdate, map);
  		}
      });
  }
}
