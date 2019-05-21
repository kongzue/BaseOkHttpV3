package com.kongzue.baseokhttp.listener;

/**
 * Created by myzcx on 2017/12/27.
 */

public interface ResponseListener extends BaseResponseListener {
    void onResponse(String response, Exception error);
}
