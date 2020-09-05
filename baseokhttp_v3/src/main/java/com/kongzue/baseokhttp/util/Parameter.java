package com.kongzue.baseokhttp.util;

/**
 * Created by myzcx on 2018/1/22.
 */

import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.util.TreeMap;

import baseokhttp3.FormBody;
import baseokhttp3.RequestBody;

import static com.kongzue.baseokhttp.util.BaseOkHttp.DEBUGMODE;

public class Parameter extends TreeMap<String, Object> {
    
    public Parameter add(String key, Object value) {
        put(key, value);
        return this;
    }
    
    public Object put(String key, Object value) {
        if (value instanceof File) {
            return super.put(key, value);
        } else {
            return super.put(key, value + "");
        }
    }
    
    public String toParameterString() {
        String result = "";
        if (!entrySet().isEmpty()) {
            for (Entry<String, Object> entry : entrySet()) {
                result = result + entry.getKey() + "=" + entry.getValue() + "&";
            }
            if (result.endsWith("&")) {
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }
    
    @Deprecated
    public void toPrintString() {
        toPrintString(0);
    }
    
    @Deprecated
    public void toPrintString(int e) {
        if (!entrySet().isEmpty()) {
            for (Entry<String, Object> entry : entrySet()) {
                if (e == 0) {
                    Log.i(">>>>>>", entry.getKey() + "=" + entry.getValue());
                } else {
                    Log.e(">>>>>>", entry.getKey() + "=" + entry.getValue());
                }
            }
        }
    }
    
    public RequestBody toOkHttpParameter() {
        RequestBody requestBody;
        
        FormBody.Builder builder = new FormBody.Builder();
        for (Entry<String, Object> entry : entrySet()) {
            builder.add(entry.getKey() + "", entry.getValue() + "");
        }
        
        requestBody = builder.build();
        return requestBody;
    }
    
    public JSONObject toParameterJson() {
        JSONObject result = new JSONObject();
        try {
            if (!entrySet().isEmpty()) {
                for (Entry<String, Object> entry : entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    public JsonMap toParameterJsonMap() {
        JsonMap result = new JsonMap();
        try {
            if (!entrySet().isEmpty()) {
                for (Entry<String, Object> entry : entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}