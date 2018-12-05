package com.kongzue.baseokhttp.exceptions;

public class NetworkErrorException extends Exception {
    public NetworkErrorException(){
        super("网络异常");
    }
}