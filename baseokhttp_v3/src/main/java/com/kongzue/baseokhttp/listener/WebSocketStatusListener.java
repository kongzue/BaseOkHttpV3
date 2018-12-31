package com.kongzue.baseokhttp.listener;

import baseokhttp3.Response;
import baseokio.ByteString;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/12/31 17:10
 */
public interface WebSocketStatusListener {
    
    void connected(Response response);
    
    void onMessage(String message);
    
    void onMessage(ByteString message);
    
    void onReconnect();
    
    void onDisconnected(int breakStatus);
    
    void onConnectionFailed(Throwable t);
    
}
