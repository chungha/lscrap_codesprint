package com.google.android.bootcamp.memegen;


import java.util.List;


/**
 * Created by chungha on 12/15/14.
 */
public interface Extractor {
    public List<String> extract(String string) throws Exception;
}
