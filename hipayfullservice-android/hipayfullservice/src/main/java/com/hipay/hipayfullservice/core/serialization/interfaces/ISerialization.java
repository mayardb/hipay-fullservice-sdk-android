package com.hipay.hipayfullservice.core.serialization.interfaces;

import java.util.Map;

/**
 * Created by nfillion on 04/02/16.
 */
public interface ISerialization {

    String getQueryString();
    Map<String,String> getSerializedRequest();
}