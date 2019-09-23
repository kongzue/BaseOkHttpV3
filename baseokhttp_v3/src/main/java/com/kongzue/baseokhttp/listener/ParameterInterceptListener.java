package com.kongzue.baseokhttp.listener;

import android.content.Context;

import com.kongzue.baseokhttp.util.Parameter;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/11/22 17:27
 */
public interface ParameterInterceptListener<Parameter> {
    Parameter onIntercept(Context context, String url, Parameter parameter);
}
