package com.kongzue.baseokhttp.listener;

import android.content.Context;

import com.kongzue.baseokhttp.util.Parameter;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/9/3 21:11
 */
public interface HeaderInterceptListener {
    
    Parameter onIntercept(Context context, String url, Parameter header);
    
}
