package com.kongzue.baseokhttp.listener;

import com.kongzue.baseokhttp.util.JsonMap;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/4/28 17:26
 */
public interface JsonResponseListener extends BaseResponseListener {
    void onResponse(JsonMap main, Exception error);
}
