package com.google.android.bootcamp.memegen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by chungha on 12/15/14.
 */
public class TagContentExtractor implements Extractor {
  @Override
  public List<String> extract(String string) {
    String html = download(string);
    Pattern p = Pattern.compile("<.*>(.+?)</.*>");
    Matcher m = p.matcher(html);
    List<String> result = new ArrayList<String>();
    while (m.find()) {
        result.add(m.group(1));
    }
    return result;
  }
  
  private String download(String url) {
	// TODO Auto-generated method stub
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
