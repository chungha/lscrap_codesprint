package com.google.android.bootcamp.memegen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Activity for creating a meme.
 */
public class CreateMemeActivity extends Activity {

  private static final String TAG = CreateMemeActivity.class.getCanonicalName();
  private Store store;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    store = new Store(this.getApplicationContext().getSharedPreferences("places_store", 0));

    setContentView(R.layout.activity_create_meme);

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
              for (String c : contents) {
                Log.d(TAG, "--- " + c);
                List<String> addresses = ke.extract(c);
                for (String a : addresses) {
                  Log.d(TAG, "+++ " + a);
                  Pair<Double, Double> p = AddressToGeoPointTranslator.run(CreateMemeActivity.this, a);
                  if (p != null) {
                    Log.d(TAG, "GeoTag OK - " + String.format("%s - %f / %f", a, p.first, p.second));

                    store.add(message, a, p.first, p.second);
                  }
                }
              }
            } catch (IOException e1) {
              e1.printStackTrace();
            }
          }
        });

      }
    }
  }
}
