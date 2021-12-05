package com.kongzue.baseokhttp.listener;

import com.kongzue.baseokhttp.util.RequestInfo;

import java.io.File;

import okhttp3.Call;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2021/12/5 11:30
 */
public abstract class CustomMimeInterceptor {
    
    public String onUploadFileMimeInterceptor(File originFile) {
        return "";
    }
    
    public String onRequestMimeInterceptor(RequestInfo requestInfo,Call call){
        return "";
    }
}
