package com.kongzue.baseokhttp.util;

import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/1/16 17:57
 */
public class JsonMap extends LinkedHashMap<String, Object> {
    
    public String getString(String key) {
        Object value = get(key);
        return value == null ? "" : value + "";
    }
    
    public int getInt(String key) {
        Object value = get(key);
        return value == null ? 0 : (int) value;
    }
    
    public boolean getBoolean(String key) {
        Object value = get(key);
        return value == null ? false : (boolean) value;
    }
    
    public long getLong(String key) {
        Object value = get(key);
        return value == null ? 0 : (long) value;
    }
    
    public short getShort(String key) {
        Object value = get(key);
        return value == null ? 0 : (short) value;
    }
    
    public double getDouble(String key) {
        Object value = get(key);
        return value == null ? 0 : (double) value;
    }
    
    public float getFloat(String key) {
        Object value = get(key);
        return value == null ? 0 : (float) value;
    }
    
    public JsonList getList(String key) {
        Object value = get(key);
        return value == null ? new JsonList() : (JsonList) value;
    }
    
    public JsonMap getJsonMap(String key) {
        Object value = get(key);
        return value == null ? new JsonMap() : (JsonMap) value;
    }
}
