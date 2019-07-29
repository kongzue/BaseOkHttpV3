package com.kongzue.baseokhttp.util;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/1/16 17:57
 */
public class JsonMap extends LinkedHashMap<String, Object> {
    
    public JsonMap() {
    
    }
    
    public String getString(String key) {
        Object value = get(key);
        return value == null ? "" : value + "";
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
    
    public JsonList getList(String key) {
        Object value = get(key);
        try {
            return value == null ? new JsonList() : (JsonList) value;
        } catch (Exception e) {
            return new JsonList();
        }
    }
    
    public JsonMap getJsonMap(String key) {
        Object value = get(key);
        try {
            return value == null ? new JsonMap() : (JsonMap) value;
        } catch (Exception e) {
            return new JsonMap();
        }
    }
    
    public JsonMap set(String key, Object value) {
        put(key, value);
        return this;
    }
    
    @Override
    public String toString() {
        return getJsonObj().toString();
    }
    
    public JSONObject getJsonObj() {
        JSONObject main = null;
        try {
            main = new JSONObject();
            
            Set<String> keys = keySet();
            for (String key : keys) {
                Object value = get(key);
                if (value instanceof JsonMap) {
                    main.put(key, ((JsonMap) value).getJsonObj());
                } else if (value instanceof JsonList) {
                    main.put(key, ((JsonList) value).getJsonArray());
                } else {
                    main.put(key, value);
                }
            }
        } catch (Exception e) {
        
        }
        return main;
    }
}
