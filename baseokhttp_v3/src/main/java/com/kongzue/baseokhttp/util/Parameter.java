package com.kongzue.baseokhttp.util;

/**
 * Created by myzcx on 2018/1/22.
 */

import android.util.Log;
import org.json.JSONObject;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class Parameter extends TreeMap<String, Object> {
    
    public Parameter add(String key, Object value) {
        put(key, value);
        return this;
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
    
    public String getString(String key) {
        return getString(key, "");
    }
    
    public String getString(String key, String defaultValue) {
        Object value = get(key);
        if (isNull(String.valueOf(value))) {
            return defaultValue;
        }
        return value == null ? "" : String.valueOf(value);
    }
    
    public int getInt(String key) {
        return getInt(key, 0);
    }
    
    public int getInt(String key, int emptyValue) {
        int result = emptyValue;
        try {
            result = Integer.parseInt(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }
    
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    
    public boolean getBoolean(String key, boolean emptyValue) {
        boolean result = emptyValue;
        try {
            result = Boolean.parseBoolean(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }
    
    public long getLong(String key) {
        return getLong(key, 0);
    }
    
    public long getLong(String key, long emptyValue) {
        long result = emptyValue;
        try {
            result = Long.parseLong(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }
    
    public short getShort(String key) {
        return getShort(key, (short) 0);
    }
    
    public short getShort(String key, short emptyValue) {
        short result = emptyValue;
        try {
            result = Short.parseShort(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }
    
    public double getDouble(String key) {
        return getDouble(key, 0);
    }
    
    public double getDouble(String key, double emptyValue) {
        double result = emptyValue;
        try {
            result = Double.parseDouble(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }
    
    public float getFloat(String key) {
        return getFloat(key, 0);
    }
    
    public float getFloat(String key, float emptyValue) {
        float result = emptyValue;
        try {
            result = Float.parseFloat(get(key) + "");
        } catch (Exception e) {
        }
        return emptyValue;
    }
    
    private boolean isNull(String s) {
        if (s == null || s.trim().isEmpty() || "null".equals(s)) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        return toString().equals(o.toString());
    }
}