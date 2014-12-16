package com.google.android.bootcamp.memegen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chungha on 12/15/14.
 */
public class KoreaAddressExtractor implements Extractor {
	private String[] firstLayerTokens;
	private String[] secondLayerPatterns;
	
	public KoreaAddressExtractor(String[] firstLayerTokens, String[] secondLayerPatterns) {
		this.firstLayerTokens = firstLayerTokens;
		this.secondLayerPatterns = secondLayerPatterns;
	}
	
    @Override
    public List<String> extract(String s) {
      Set<String> result = new HashSet<String>();
      for (String token : firstLayerTokens) {
        if (s.contains(token)) {
          for (String p : secondLayerPatterns) {
            Pattern pattern = Pattern.compile(p);
            Matcher matcher = pattern.matcher(s);
            while (matcher.find()) {
              result.add(s.substring(matcher.start(), matcher.end()).trim());
            }
          }
        }
      }
      return new ArrayList<String>(result);
    }
}
