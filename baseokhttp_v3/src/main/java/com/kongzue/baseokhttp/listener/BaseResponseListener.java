package com.kongzue.baseokhttp.listener;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/4/28 17:29
 */
public interface BaseResponseListener {
    
    /**
     * 此方法用于内部传递未处理的返回值，请使用 ResponseListener 的具体实现
     * @hide
     * @param response
     * @param error
     */
    void response(Object response, Exception error);
}
