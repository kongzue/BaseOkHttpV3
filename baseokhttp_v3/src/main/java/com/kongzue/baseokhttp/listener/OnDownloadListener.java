package com.kongzue.baseokhttp.listener;

import java.io.File;

import okhttp3.Response;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/5/19 15:33
 */
public abstract class OnDownloadListener {
    
    public abstract void onDownloadSuccess(File file);
    
    public abstract void onDownloading(int progress);
    
    public abstract void onDownloadFailed(Exception e);

    public abstract void onDownloadBegin(Response response,long totalContentLength);
}
