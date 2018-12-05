package com.kongzue.baseokhttp.exceptions;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/11/20 21:06
 */
public class TimeOutException extends Exception {
    public TimeOutException(){
        super("请求超时");
    }
}