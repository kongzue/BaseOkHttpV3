package com.kongzue.baseokhttp.listener;

import com.kongzue.baseokhttp.exceptions.DecodeJsonException;
import com.kongzue.baseokhttp.util.JsonMap;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/4/28 17:26
 */
public abstract class JsonResponseListener implements BaseResponseListener {
    
    @Override
    public void response(Object response, Exception error) {
        if (error == null) {
            JsonMap data = new JsonMap(response.toString());
            if (data!=null && !data.isEmpty()) {
                onResponse(data, error);
            } else {
                onResponse(new JsonMap(), new DecodeJsonException(response.toString()));
            }
        } else {
            onResponse(new JsonMap(), error);
        }
    }
    
    public abstract void onResponse(JsonMap main, Exception error);
}
