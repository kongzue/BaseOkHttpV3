package com.kongzue.baseokhttp.util;


import java.util.ArrayList;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/1/16 18:16
 */
public class JsonList extends ArrayList {
    
    public String getString(int index) {
        Object value = get(index);
        return value == null ? "" : value + "";
    }
    
    public int getInt(int index) {
        Object value = get(index);
        return value == null ? 0 : (int) value;
    }
    
    public boolean getBoolean(int index) {
        Object value = get(index);
        return value == null ? false : (boolean) value;
    }
    
    public long getLong(int index) {
        Object value = get(index);
        return value == null ? 0 : (long) value;
    }
    
    public short getShort(int index) {
        Object value = get(index);
        return value == null ? 0 : (short) value;
    }
    
    public double getDouble(int index) {
        Object value = get(index);
        return value == null ? 0 : (double) value;
    }
    
    public float getFloat(int index) {
        Object value = get(index);
        return value == null ? 0 : (float) value;
    }
    
    public JsonList getList(int index) {
        Object value = get(index);
        return value == null ? new JsonList() : (JsonList) value;
    }
    
    public JsonMap getJsonMap(int index) {
        Object value = get(index);
        return value == null ? new JsonMap() : (JsonMap) value;
    }
    
}
