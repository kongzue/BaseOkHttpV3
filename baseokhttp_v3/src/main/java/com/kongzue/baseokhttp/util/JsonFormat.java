package com.kongzue.baseokhttp.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kongzue.baseokhttp.util.BaseOkHttp.DEBUGMODE;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/11/9 22:06
 */
public class JsonFormat {
    
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    public static List<LockLog.LogBody> formatJson(String msg) {
        return formatJson(msg, 0);
    }
    
    public static List<LockLog.LogBody> formatJson(String msg, int e) {
        if (!DEBUGMODE) return null;
        String message;
        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(4);//最重要的方法，就一行，返回格式化的json字符串，其中的数字4是缩进字符数
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(4);
            } else {
                return null;
            }
        } catch (JSONException err) {
            return null;
        }
        
        String[] lines = message.split(LINE_SEPARATOR);
        List<LockLog.LogBody> logBodyList = new ArrayList<>();
        for (String line : lines) {
            if (e == 0) {
                logBodyList.add(new LockLog.LogBody(LockLog.LogBody.LEVEL.INFO, ">>>>>>", line));
            } else {
                logBodyList.add(new LockLog.LogBody(LockLog.LogBody.LEVEL.ERROR, ">>>>>>", line));
            }
        }
        return logBodyList;
    }
}
