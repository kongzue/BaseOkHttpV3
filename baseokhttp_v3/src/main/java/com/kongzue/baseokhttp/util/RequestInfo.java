package com.kongzue.baseokhttp.util;

import java.util.Objects;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/11/4 12:48
 */
public class RequestInfo {
    
    private String url;
    
    private String parameter;
    
    public RequestInfo(String url, String parameter) {
        this.url = url;
        this.parameter = parameter;
    }
    
    public RequestInfo(String url, Parameter parameter) {
        this.url = url;
        this.parameter = parameter == null ? "" : parameter.toParameterString();
    }
    
    public String getUrl() {
        return url;
    }
    
    public RequestInfo setUrl(String url) {
        this.url = url;
        return this;
    }
    
    public String getParameter() {
        return parameter;
    }
    
    public RequestInfo setParameter(String parameter) {
        this.parameter = parameter;
        return this;
    }
    
    public boolean equals(RequestInfo requestInfo) {
        if (this == requestInfo) return true;
        if (requestInfo == null || getClass() != requestInfo.getClass()) return false;
        return equalsString(url, requestInfo.url) && equalsString(parameter, requestInfo.parameter);
    }
    
    private boolean equalsString(String a, String b) {
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    @Override
    public String toString() {
        return "RequestInfo{" +
                "url='" + url + '\'' +
                ", parameter='" + parameter + '\'' +
                '}';
    }
}
