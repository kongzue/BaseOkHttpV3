package com.kongzue.baseokhttp.listener;

import android.content.Context;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/7/5 18:24
 */
public interface ResponseInterceptListener {
    boolean onResponse(Context context, String url, String response, Exception error);
}
