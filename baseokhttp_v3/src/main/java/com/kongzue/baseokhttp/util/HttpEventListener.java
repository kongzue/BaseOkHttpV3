package com.kongzue.baseokhttp.util;

import android.util.Log;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2021/11/14 14:08
 */
public class HttpEventListener  extends EventListener {
    
    public static final Factory FACTORY = new Factory() {
        final AtomicLong nextCallId = new AtomicLong(1L);
        
        @Override
        public EventListener create(Call call) {
            long callId = nextCallId.getAndIncrement();
            return new HttpEventListener(callId, call.request().url(), System.nanoTime());
        }
    };
    
    private final long callId;
    
    private final long callStartNanos;
    
    LockLog.Builder logBuilder = LockLog.Builder.create();
    
    public HttpEventListener(long callId, HttpUrl url, long callStartNanos) {
        this.callId = callId;
        this.callStartNanos = callStartNanos;
    }
    
    private void recordEventLog(String name) {
        long elapseNanos = System.nanoTime() - callStartNanos;
    
        logBuilder.i("***",
                (elapseNanos / 1000000) + "ms: \t#"+ name
                );
        if (name.equalsIgnoreCase("请求结束") || name.equalsIgnoreCase("请求失败 ×")) {
            logBuilder.build();
        }
    }
    
    @Override
    public void callStart(Call call) {
        super.callStart(call);
        recordEventLog("请求开始：" + call.request().url());
    }
    
    @Override
    public void dnsStart(Call call, String domainName) {
        super.dnsStart(call, domainName);
        recordEventLog("DNS 解析开始");
    }
    
    @Override
    public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
        super.dnsEnd(call, domainName, inetAddressList);
        recordEventLog("DNS 解析结束");
    }
    
    @Override
    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
        super.connectStart(call, inetSocketAddress, proxy);
        recordEventLog("连接开始");
    }
    
    @Override
    public void secureConnectStart(Call call) {
        super.secureConnectStart(call);
        recordEventLog("安全连接开始（HTTPS）");
    }
    
    @Override
    public void secureConnectEnd(Call call, @Nullable Handshake handshake) {
        super.secureConnectEnd(call, handshake);
        recordEventLog("安全连接结束（HTTPS）");
    }
    
    @Override
    public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol);
        recordEventLog("连接结束");
    }
    
    @Override
    public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol, IOException ioe) {
        super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
        recordEventLog("连接失败");
    }
    
    @Override
    public void connectionAcquired(Call call, Connection connection) {
        super.connectionAcquired(call, connection);
        recordEventLog("连接获得");
    }
    
    @Override
    public void connectionReleased(Call call, Connection connection) {
        super.connectionReleased(call, connection);
        recordEventLog("连接释放");
    }
    
    @Override
    public void requestHeadersStart(Call call) {
        super.requestHeadersStart(call);
        recordEventLog("请求头开始");
    }
    
    @Override
    public void requestHeadersEnd(Call call, Request request) {
        super.requestHeadersEnd(call, request);
        recordEventLog("请求头结束");
    }
    
    @Override
    public void requestBodyStart(Call call) {
        super.requestBodyStart(call);
        recordEventLog("请求体开始");
    }
    
    @Override
    public void requestBodyEnd(Call call, long byteCount) {
        super.requestBodyEnd(call, byteCount);
        recordEventLog("请求体结束");
    }
    
    @Override
    public void responseHeadersStart(Call call) {
        super.responseHeadersStart(call);
        recordEventLog("响应头开始");
    }
    
    @Override
    public void responseHeadersEnd(Call call, Response response) {
        super.responseHeadersEnd(call, response);
        recordEventLog("响应头结束");
    }
    
    @Override
    public void responseBodyStart(Call call) {
        super.responseBodyStart(call);
        recordEventLog("响应体开始");
    }
    
    @Override
    public void responseBodyEnd(Call call, long byteCount) {
        super.responseBodyEnd(call, byteCount);
        recordEventLog("响应体结束");
    }
    
    @Override
    public void callEnd(Call call) {
        super.callEnd(call);
        recordEventLog("请求结束");
    }
    
    @Override
    public void callFailed(Call call, IOException ioe) {
        super.callFailed(call, ioe);
        recordEventLog("请求失败 ×");
    }
}
