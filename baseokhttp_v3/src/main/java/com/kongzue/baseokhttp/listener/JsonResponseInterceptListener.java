package com.kongzue.baseokhttp.listener;

import android.content.Context;

import com.kongzue.baseokhttp.util.JsonMap;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/8/28 15:08
 */
public abstract class JsonResponseInterceptListener implements BaseResponseInterceptListener {
    
    @Override
    public boolean response(Context context, String url, String response, Exception error) {
        return onResponse(context, url, new JsonMap(response), error);
    }
    
    public abstract boolean onResponse(Context context, String url, JsonMap response, Exception error);
}
