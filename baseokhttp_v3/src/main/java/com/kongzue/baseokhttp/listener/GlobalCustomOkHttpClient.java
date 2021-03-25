package com.kongzue.baseokhttp.listener;

import com.kongzue.baseokhttp.HttpRequest;

import okhttp3.OkHttpClient;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/12/9 14:58
 */
public interface GlobalCustomOkHttpClient {
    
    OkHttpClient customBuilder(HttpRequest request, OkHttpClient builder);
}
