package com.kongzue.baseokhttp.listener;

import okhttp3.MultipartBody;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/9/9 13:11
 */
public interface MultipartBuilderInterceptor {
    
    MultipartBody.Builder interceptMultipartBuilder(MultipartBody.Builder multipartBuilder);
}
