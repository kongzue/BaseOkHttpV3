package com.kongzue.baseokhttp.listener;

import com.kongzue.baseokhttp.util.JsonMap;

/**
 * Created by myzcx on 2017/12/27.
 */

public abstract class ResponseListener implements BaseResponseListener {
    
    @Override
    public void response(Object response, Exception error) {
        onResponse(response == null ? "" : response.toString(), error);
    }
    
    public abstract void onResponse(String main, Exception error);
}
