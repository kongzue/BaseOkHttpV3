package com.kongzue.baseokhttp.listener;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/9/5 11:23
 */
public interface UploadProgressListener {
    
    /**
     * 上传回调监听
     *
     * @param percentage 百分比
     * @param current    当前（字节长度）
     * @param total      总量（字节长度）
     * @param done       是否已完成
     */
    void onUpload(float percentage, long current, long total, boolean done);
}
