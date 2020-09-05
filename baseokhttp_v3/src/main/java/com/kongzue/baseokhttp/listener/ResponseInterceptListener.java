package com.kongzue.baseokhttp.listener;

import android.content.Context;

/**
 * 全局请求回调拦截器
 * 回调为 JsonMap 对象请使用 JsonResponseInterceptListener
 * 回调为 JavaBean 对象请使用 BeanResponseInterceptListener
 *
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/7/5 18:24
 */
public abstract class ResponseInterceptListener implements BaseResponseInterceptListener {
    
    @Override
    public boolean response(Context context, String url, String response, Exception error) {
        return onResponse(context, url, response, error);
    }
    
    public abstract boolean onResponse(Context context, String url, String response, Exception error);
}
