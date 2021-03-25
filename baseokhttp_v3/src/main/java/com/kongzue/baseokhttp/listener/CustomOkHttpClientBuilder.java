package com.kongzue.baseokhttp.listener;

import okhttp3.OkHttpClient;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/12/9 14:45
 */
public interface CustomOkHttpClientBuilder {
    
    OkHttpClient.Builder customBuilder(OkHttpClient.Builder builder);
}
