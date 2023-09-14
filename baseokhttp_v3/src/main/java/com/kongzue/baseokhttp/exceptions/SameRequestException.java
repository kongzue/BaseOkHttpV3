package com.kongzue.baseokhttp.exceptions;

public class SameRequestException extends Exception {
    public SameRequestException(String errorInfo) {
        super(errorInfo);
    }
}