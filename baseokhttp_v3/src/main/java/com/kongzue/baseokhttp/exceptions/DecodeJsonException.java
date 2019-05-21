package com.kongzue.baseokhttp.exceptions;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2019/5/18 22:21
 */
public class DecodeJsonException extends Exception {
    public DecodeJsonException(String errorInfo){
        super("Json解析失败：\n" + errorInfo);
    }
}
