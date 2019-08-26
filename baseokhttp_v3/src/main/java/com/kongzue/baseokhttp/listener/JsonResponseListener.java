package com.kongzue.baseokhttp.listener;

import com.kongzue.baseokhttp.exceptions.DecodeJsonException;
import com.kongzue.baseokhttp.util.JsonMap;
import com.kongzue.baseokhttp.util.JsonUtil;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/4/28 17:26
 */
public abstract class JsonResponseListener implements BaseResponseListener {
    
    @Override
    public void onResponse(Object response, Exception error) {
        if (error == null) {
            JsonMap data = JsonUtil.deCodeJsonObject(response.toString());
            if (!data.isEmpty()) {
                onResponse(data, error);
            } else {
                onResponse(data, new DecodeJsonException(response.toString()));
            }
        } else {
            onResponse(null, error);
        }
    }
    
    public abstract void onResponse(JsonMap main, Exception error);
}
