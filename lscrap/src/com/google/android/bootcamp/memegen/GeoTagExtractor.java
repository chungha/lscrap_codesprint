package com.google.android.bootcamp.memegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts location information(geotag) embedded in Google blog URL.
 */
public class GeoTagExtractor {

  private static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
  private static final String HTML_A_HREF_TAG_PATTERN = 
      "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
  private static final String HTML_LATLON_PATTERN = "@([\\d.]+),([\\d.]+)";

  private static final Pattern PATTERN_TAG = Pattern.compile(HTML_A_TAG_PATTERN);
  private static final Pattern PATTERN_LINK = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
  private static final Pattern PATTERN_LATLON = Pattern.compile(HTML_LATLON_PATTERN);

  public static List<GeoTag> getGeoTagsFromHtml(String html) {
    Matcher matcherTag, matcherLink;
    matcherTag = PATTERN_TAG.matcher(html);
    ArrayList<GeoTag> res = new ArrayList<GeoTag>();
    while (matcherTag.find()) {
      String href = matcherTag.group(1); // href
      String linkText = matcherTag.group(2); // link text
      matcherLink = PATTERN_LINK.matcher(href);

      while (matcherLink.find()) {
        String link = matcherLink.group(1); // link
        if (link.indexOf("http://maps.google.com/maps?") == -1) continue;
        Matcher gp = PATTERN_LATLON.matcher(link);
        if (!gp.find()) continue;
        System.out.println("Geopoint:" + Double.valueOf(gp.group(1)) + " / " + gp.group(2));
        res.add(new GeoTag(linkText, Double.valueOf(gp.group(1)), Double.valueOf(gp.group(2))));
        System.out.println("linkText:" + linkText + "\nlink:" + link);
      }
    }
    return res.isEmpty() ? Collections.<GeoTag>emptyList() : res;
  }

  public static void main(String[] args) {
    getGeoTagsFromHtml(
    "<span class='post-location'>" +
    "Attached Location:" +
    "<a href='http://maps.google.com/maps?q=Sujeong-dong,+Yeosu-si,+Jeollanam-do," +
    "+South+Korea@34.74126228589686,127.7534601218506&z=10' target='_blank'>Sujeong-dong," +
    " Yeosu-si, Jeollanam-do, South Korea</a>" +
    "</span>"
    );
  }
}
