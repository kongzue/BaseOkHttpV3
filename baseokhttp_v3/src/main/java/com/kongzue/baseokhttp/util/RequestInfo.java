package com.kongzue.baseokhttp.util;

import com.kongzue.baseokhttp.listener.ResponseListener;

import java.util.ArrayList;
import java.util.List;
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
    private int contextHash;

    private List<ResponseListener> sameRequestCallbacks = new ArrayList<ResponseListener>();

    public RequestInfo(String url, String parameter, int contextHash) {
        this.url = url;
        this.parameter = parameter;
        this.contextHash = contextHash;
    }

    public RequestInfo(String url, Parameter parameter, int contextHash) {
        this.url = url;
        this.parameter = parameter == null ? "" : parameter.toParameterString();
        this.contextHash = contextHash;
    }

    public String getUrl() {
        return url;
    }

    public RequestInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public int getContextHash() {
        return contextHash;
    }

    public RequestInfo setContextHash(int contextHash) {
        this.contextHash = contextHash;
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
        return equalsString(url, requestInfo.url) && equalsString(parameter, requestInfo.parameter) && contextHash == requestInfo.contextHash;
    }

    private boolean equalsString(String a, String b) {
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    public void addSameRequestCallback(ResponseListener listener) {
        sameRequestCallbacks.add(listener);
    }

    public List<ResponseListener> getSameRequestCallbacks() {
        return sameRequestCallbacks;
    }

    @Override
    public String toString() {
        return "RequestInfo{" +
                "url='" + url + '\'' +
                ", parameter='" + parameter + '\'' +
                '}';
    }
}
