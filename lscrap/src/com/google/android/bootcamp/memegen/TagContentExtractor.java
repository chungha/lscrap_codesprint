package com.google.android.bootcamp.memegen;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chungha on 12/15/14.
 */
public class TagContentExtractor implements Extractor {
  private static final String TAG = TagContentExtractor.class.getCanonicalName();

@Override
  public List<String> extract(String string) {
    return downloadAndRemoveTags(string);
  }
  
  private List<String> downloadAndRemoveTags(String url) {
	  List<String> result = new ArrayList<String>();
      HttpClient client = new DefaultHttpClient();
      HttpGet request = new HttpGet(url);
      try {
          HttpResponse response = client.execute(request);
          InputStream in;
          in = response.getEntity().getContent();
          BufferedReader reader = new BufferedReader(
                  new InputStreamReader(in));
          String line = null;
          while ((line = reader.readLine()) != null) {
        	  result.add(line.trim().replaceAll("\\<.*?>",""));
          }
          in.close();
      } catch (IllegalStateException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
      return result;
  }	
}
