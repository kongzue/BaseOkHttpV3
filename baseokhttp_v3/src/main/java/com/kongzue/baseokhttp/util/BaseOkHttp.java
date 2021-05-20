package com.kongzue.baseokhttp.util;

import android.util.Log;

import com.kongzue.baseokhttp.listener.BaseResponseInterceptListener;
import com.kongzue.baseokhttp.listener.GlobalCustomOkHttpClient;
import com.kongzue.baseokhttp.listener.GlobalCustomOkHttpClientBuilder;
import com.kongzue.baseokhttp.listener.HeaderInterceptListener;
import com.kongzue.baseokhttp.listener.ParameterInterceptListener;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/12/5 17:48
 */
public class BaseOkHttp {
    
    //请求类型
    public static final int POST_REQUEST = 0;       //普通POST
    public static final int GET_REQUEST = 1;        //普通GET
    public static final int PUT_REQUEST = 2;
    public static final int DELETE_REQUEST = 3;
    public static final int DOWNLOAD = 4;
    public static final int PATCH_REQUEST = 5;
    
    //是否开启调试模式
    public static boolean DEBUGMODE = false;
    
    //超时时长（单位：秒）
    public static int TIME_OUT_DURATION = 10;
    
    //默认服务器地址
    public static String serviceUrl = "";
    
    //Https请求需要传入Assets目录下的证书文件名称
    public static String SSLInAssetsFileName;
    
    //Https请求是否需要Hostname验证，请保证serviceUrl中即Hostname地址
    public static boolean httpsVerifyServiceUrl = false;
    
    //全局拦截器
    public static BaseResponseInterceptListener responseInterceptListener;
    
    //全局参数拦截器
    public static ParameterInterceptListener parameterInterceptListener;
    
    //全局Header拦截器
    public static HeaderInterceptListener headerInterceptListener;
    
    //全局请求头
    public static Parameter overallHeader;
    
    //全局参数
    public static Parameter overallParameter;
    
    //WebSocket自动重连步长（单位：秒）
    public static int websocketReconnectInterval = 10;
    
    //WebSocket最大重连间隔（单位：秒）
    public static int websocketReconnectTime = 120;
    
    //自动缓存 Cookies
    public static boolean autoSaveCookies;
    
    //已存储的 Cookie 队列
    protected HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();
    
    //容灾地址
    public static String[] reserveServiceUrls;
    
    //是否开启详细日志信息（下载、线程提升信息）
    public static boolean DETAILSLOGS = false;
    
    //全局代理设置
    public static Proxy proxy;
    
    //禁止重复请求
    public static boolean disallowSameRequest;
    
    //缓存请求
    public static boolean requestCache = true;
    
    //全局自定义 OkHttpClient
    public static GlobalCustomOkHttpClient globalCustomOkHttpClient;
    
    //全局自定义 OkHttpClientBuilder
    public static GlobalCustomOkHttpClientBuilder globalCustomOkHttpClientBuilder;
    
    public static void cleanSameRequestList() {
        requestInfoList = new ArrayList<>();
    }
    
    private static List<RequestInfo> requestInfoList;
    
    protected void addRequestInfo(RequestInfo requestInfo) {
        synchronized (BaseOkHttp.class) {
            if (requestInfoList == null) {
                requestInfoList = new ArrayList<>();
            }
            if (DEBUGMODE) Log.i(">>>", "addRequestInfo: " + requestInfo);
            requestInfoList.add(requestInfo);
        }
    }
    
    protected void deleteRequestInfo(RequestInfo requestInfo) {
        synchronized (BaseOkHttp.class) {
            if (requestInfoList == null || requestInfoList.isEmpty() || requestInfo == null) {
                return;
            }
            requestInfoList.remove(requestInfo);
        }
    }
    
    protected boolean equalsRequestInfo(RequestInfo requestInfo) {
        synchronized (BaseOkHttp.class) {
            if (requestInfoList == null || requestInfoList.isEmpty()) {
                if (DEBUGMODE) Log.d(">>>", "requestInfoList: null");
                return false;
            }
            for (RequestInfo requestInfo1 : requestInfoList) {
                if (DEBUGMODE) Log.d(">>>", "equalsRequestInfo: " + requestInfo1);
                if (requestInfo1.equals(requestInfo)) {
                    if (DEBUGMODE) {
                        Log.w(">>>", "发生重复请求: " + requestInfo);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
