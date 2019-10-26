package com.kongzue.baseokhttp.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/1/16 17:23
 */
public class JsonUtil {
    
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    public static Object deCodeJson(String jsonStr) {
        jsonStr = jsonStr.replace(LINE_SEPARATOR, "");
        JsonMap result = new JsonMap();
        try {
            if (jsonStr.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(jsonStr);
                Iterator keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next() + "";
                    String value = jsonObject.optString(key);
                    if (value.startsWith("{")) {
                        JsonMap object = deCodeJsonObject(value);
                        result.put(key, object == null ? value : object);
                    } else if (value.startsWith("[")) {
                        JsonList array = deCodeJsonArray(value);
                        result.put(key, array == null ? value : array);
                    } else {
                        result.put(key, value);
                    }
                }
                return result;
            } else if (jsonStr.startsWith("[")) {
                return deCodeJsonArray(jsonStr);
            } else {
                loge("参数不是一个合法的json：" + jsonStr);
                return jsonStr;
            }
        } catch (Exception e) {
            loge("参数不是一个合法的json：" + jsonStr);
            return jsonStr;
        }
    }
    
    public static JsonMap deCodeJsonObject(String jsonStr) {
        try {
            return (JsonMap) deCodeJson(jsonStr);
        } catch (Exception e) {
            loge("参数不是一个合法的jsonObject：" + jsonStr);
            return null;
        }
    }
    
    public static JsonList deCodeJsonArray(String jsonStr) {
        JsonList result = new JsonList();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                Object o = jsonArray.get(i) + "";
                if ((o + "").startsWith("{") || (o + "").startsWith("[")) {
                    result.add(deCodeJson(o + ""));
                } else {
                    result.add(o);
                }
            }
            return result;
        } catch (Exception e) {
            loge("不是一个合法的json：" + jsonStr);
            return null;
        }
    }
    
    private static void loge(String s) {
        if (BaseOkHttp.DEBUGMODE) {
            Log.e(">>>", s);
        }
    }
    
    private JsonUtil() {
    
    }
}
