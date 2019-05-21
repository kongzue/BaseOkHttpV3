package com.kongzue.baseokhttp.listener;

import java.io.File;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/5/19 15:33
 */
public interface OnDownloadListener {
    
    void onDownloadSuccess(File file);
    
    void onDownloading(int progress);
    
    void onDownloadFailed(Exception e);
}
