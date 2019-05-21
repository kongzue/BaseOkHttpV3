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
    
    private String jsonStr = "";
    
    public JsonList() {
    
    }
    
    public JsonList(String jsonStr) {
        this.jsonStr = jsonStr;
    }
    
    public String getString(int index) {
        Object value = get(index);
        return value == null ? "" : value + "";
    }

    public int getInt(int index) {
        return getInt(index, 0);
    }

    public int getInt(int index, int emptyValue) {
        int result = emptyValue;
        try {
            result = Integer.parseInt(get(index) + "");
        } catch (Exception e) {
        }
        return result;
    }

    public boolean getBoolean(int index) {
        return getBoolean(index, false);
    }

    public boolean getBoolean(int index, boolean emptyValue) {
        boolean result = emptyValue;
        try {
            result = Boolean.parseBoolean(get(index) + "");
        } catch (Exception e) {
        }
        return result;
    }

    public long getLong(int index) {
        return getLong(index, 0);
    }

    public long getLong(int index, long emptyValue) {
        long result = emptyValue;
        try {
            result = Long.parseLong(get(index) + "");
        } catch (Exception e) {
        }
        return result;
    }

    public short getShort(int index) {
        return getShort(index, (short) 0);
    }

    public short getShort(int index, short emptyValue) {
        short result = emptyValue;
        try {
            result = Short.parseShort(get(index) + "");
        } catch (Exception e) {
        }
        return result;
    }

    public double getDouble(int index) {
        return getDouble(index, 0);
    }

    public double getDouble(int index, double emptyValue) {
        double result = emptyValue;
        try {
            result = Double.parseDouble(get(index) + "");
        } catch (Exception e) {
        }
        return result;
    }

    public float getFloat(int index) {
        return getFloat(index, 0);
    }

    public float getFloat(int index, float emptyValue) {
        float result = emptyValue;
        try {
            result = Float.parseFloat(get(index) + "");
        } catch (Exception e) {
        }
        return emptyValue;
    }
    
    public JsonList getList(int index) {
        Object value = get(index);
        try {
            return value == null ? new JsonList() : (JsonList) value;
        }catch (Exception e){
            return new JsonList();
        }
    }
    
    public JsonMap getJsonMap(int index) {
        Object value = get(index);
        try {
            return value == null ? new JsonMap() : (JsonMap) value;
        }catch (Exception e){
            return new JsonMap();
        }
    }
    
    public JsonList set(Object value) {
        super.add(value);
        return this;
    }
    
    @Override
    public String toString() {
        return jsonStr;
    }
}
