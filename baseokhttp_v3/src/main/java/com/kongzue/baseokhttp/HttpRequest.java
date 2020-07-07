package com.kongzue.baseokhttp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.kongzue.baseokhttp.exceptions.DecodeJsonException;
import com.kongzue.baseokhttp.exceptions.TimeOutException;
import com.kongzue.baseokhttp.listener.BaseResponseListener;
import com.kongzue.baseokhttp.listener.JsonResponseListener;
import com.kongzue.baseokhttp.listener.OnDownloadListener;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.baseokhttp.util.BaseOkHttp;
import com.kongzue.baseokhttp.util.JsonFormat;
import com.kongzue.baseokhttp.util.JsonList;
import com.kongzue.baseokhttp.util.JsonMap;
import com.kongzue.baseokhttp.util.JsonUtil;
import com.kongzue.baseokhttp.util.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import baseokhttp3.Cache;
import baseokhttp3.Call;
import baseokhttp3.Callback;
import baseokhttp3.Cookie;
import baseokhttp3.CookieJar;
import baseokhttp3.HttpUrl;
import baseokhttp3.MediaType;
import baseokhttp3.MultipartBody;
import baseokhttp3.OkHttpClient;
import baseokhttp3.RequestBody;
import baseokhttp3.Response;

//待办：单独设置某一次请求超时时间

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2018/12/5 17:25
 */
public class HttpRequest extends BaseOkHttp {
    
    private OkHttpClient okHttpClient;
    private Call httpCall;
    private MediaType MEDIA_TYPE = MediaType.parse("image/png");
    
    private Parameter parameter;
    private Parameter headers;
    private WeakReference<Context> context;
    private HttpRequest httpRequest;
    private BaseResponseListener responseListener;
    private String requestUrl;
    private String jsonParameter;
    private String stringParameter;
    private int timeoutDuration = TIME_OUT_DURATION;
    
    private String cookieStr;
    
    private int requestType;
    
    private boolean isSending;
    
    //POST一步创建方法
    public static void POST(Context context, String url, Parameter parameter, BaseResponseListener listener) {
        POST(context, url, null, parameter, listener);
    }
    
    //POST一步创建总方法
    public static void POST(Context context, String url, Parameter headers, Parameter parameter, BaseResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = new WeakReference<Context>(context);
            httpRequest.headers = headers;
            httpRequest.responseListener = listener;
            httpRequest.parameter = parameter;
            httpRequest.requestUrl = url;
            httpRequest.requestType = POST_REQUEST;
            httpRequest.httpRequest = httpRequest;
            httpRequest.send();
        }
    }
    
    //JSON格式POST一步创建方法
    public static void JSONPOST(Context context, String url, String jsonParameter, BaseResponseListener listener) {
        JSONPOST(context, url, null, jsonParameter, listener);
    }
    
    //JSON格式POST一步创建总方法
    public static void JSONPOST(Context context, String url, Parameter headers, String jsonParameter, BaseResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = new WeakReference<Context>(context);
            httpRequest.headers = headers;
            httpRequest.responseListener = listener;
            httpRequest.jsonParameter = jsonParameter;
            httpRequest.requestUrl = url;
            httpRequest.requestType = POST_REQUEST;
            httpRequest.httpRequest = httpRequest;
            httpRequest.send();
        }
    }
    
    //String文本POST一步创建方法
    public static void StringPOST(Context context, String url, String stringParameter, BaseResponseListener listener) {
        StringPOST(context, url, null, stringParameter, listener);
    }
    
    //String文本POST一步创建总方法
    public static void StringPOST(Context context, String url, Parameter headers, String stringParameter, BaseResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = new WeakReference<Context>(context);
            httpRequest.headers = headers;
            httpRequest.responseListener = listener;
            httpRequest.stringParameter = stringParameter;
            httpRequest.requestUrl = url;
            httpRequest.requestType = POST_REQUEST;
            httpRequest.httpRequest = httpRequest;
            httpRequest.send();
        }
    }
    
    //GET一步创建方法
    public static void GET(Context context, String url, Parameter parameter, BaseResponseListener listener) {
        GET(context, url, null, parameter, listener);
    }
    
    //GET一步创建总方法
    public static void GET(Context context, String url, Parameter headers, Parameter parameter, BaseResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = new WeakReference<Context>(context);
            httpRequest.headers = headers;
            httpRequest.responseListener = listener;
            httpRequest.parameter = parameter;
            httpRequest.requestUrl = url;
            httpRequest.requestType = GET_REQUEST;
            httpRequest.httpRequest = httpRequest;
            httpRequest.send();
        }
    }
    
    //PUT一步创建方法
    public static void PUT(Context context, String url, Parameter parameter, BaseResponseListener listener) {
        PUT(context, url, null, parameter, listener);
    }
    
    //PUT一步创建总方法
    public static void PUT(Context context, String url, Parameter headers, Parameter parameter, BaseResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = new WeakReference<Context>(context);
            httpRequest.headers = headers;
            httpRequest.responseListener = listener;
            httpRequest.parameter = parameter;
            httpRequest.requestUrl = url;
            httpRequest.requestType = PUT_REQUEST;
            httpRequest.httpRequest = httpRequest;
            httpRequest.send();
        }
    }
    
    //DELETE一步创建方法
    public static void DELETE(Context context, String url, Parameter parameter, BaseResponseListener listener) {
        DELETE(context, url, null, parameter, listener);
    }
    
    //PUT一步创建总方法
    public static void DELETE(Context context, String url, Parameter headers, Parameter parameter, BaseResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = new WeakReference<Context>(context);
            httpRequest.headers = headers;
            httpRequest.responseListener = listener;
            httpRequest.parameter = parameter;
            httpRequest.requestUrl = url;
            httpRequest.requestType = DELETE_REQUEST;
            httpRequest.httpRequest = httpRequest;
            httpRequest.send();
        }
    }
    
    //DOWNLOAD一步创建
    public static void DOWNLOAD(Context context, String url, File downloadFile, OnDownloadListener onDownloadListener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = new WeakReference<Context>(context);
            httpRequest.requestUrl = url;
            httpRequest.doDownload(downloadFile, onDownloadListener);
        }
    }
    
    private boolean isFileRequest = false;
    private boolean isJsonRequest = false;
    private boolean isStringRequest = false;
    private boolean skipSSLCheck = false;
    
    private String url;
    
    private void send() {
        timeoutDuration = TIME_OUT_DURATION;
        isFileRequest = false;
        isJsonRequest = false;
        isStringRequest = false;
        
        if (parameter != null && !parameter.entrySet().isEmpty()) {
            for (Map.Entry<String, Object> entry : parameter.entrySet()) {
                if (entry.getValue() instanceof File) {
                    isFileRequest = true;
                    break;
                }
            }
        }
        if (!isNull(jsonParameter)) {
            isJsonRequest = true;
            isStringRequest = false;
        }
        if (!isNull(stringParameter)) {
            isStringRequest = true;
            isJsonRequest = false;
        }
        
        try {
            if (parameter == null) {
                parameter = new Parameter();
            }
            
            if (!requestUrl.startsWith("http")) {
                url = getRealRequestUrl(requestUrl);
            } else {
                url = requestUrl;
            }
            if (isNull(url)) {
                Log.e(">>>", "-------------------------------------");
                Log.e(">>>", "创建请求失败: 请求地址不能为空");
                Log.e(">>>", "=====================================");
                return;
            }
            
            //全局参数
            if (overallParameter != null && !overallParameter.entrySet().isEmpty()) {
                for (Map.Entry<String, Object> entry : overallParameter.entrySet()) {
                    parameter.add(entry.getKey(), entry.getValue());
                }
            }
            
            if (!skipSSLCheck && SSLInAssetsFileName != null && !SSLInAssetsFileName.isEmpty()) {
                okHttpClient = getOkHttpClient(context.get(), context.get().getAssets().open(SSLInAssetsFileName));
            } else {
                okHttpClient = new OkHttpClient.Builder()
                        .retryOnConnectionFailure(false)
                        .connectTimeout(timeoutDuration, TimeUnit.SECONDS)
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        })
                        .build();
            }
            
            //创建请求
            baseokhttp3.Request request;
            baseokhttp3.Request.Builder builder = new baseokhttp3.Request.Builder();
            
            RequestBody requestBody = null;
            
            if (isFileRequest) {
                if (parameterInterceptListener != null) {
                    try {
                        parameter = (Parameter) parameterInterceptListener.onIntercept(context.get(), url, parameter);
                    } catch (Exception e) {
                    }
                }
                
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                
                if (parameter != null && !parameter.entrySet().isEmpty()) {
                    for (Map.Entry<String, Object> entry : parameter.entrySet()) {
                        if (entry.getValue() instanceof File) {
                            File file = (File) entry.getValue();
                            multipartBuilder.addFormDataPart(entry.getKey(), file.getName(), RequestBody.create(MEDIA_TYPE, file));
                            if (DEBUGMODE) {
                                Log.i(">>>", "添加文件：" + entry.getKey() + ":" + file.getName());
                            }
                        } else if (entry.getValue() instanceof List) {
                            List valueList = (List) entry.getValue();
                            for (Object value : valueList) {
                                if (value instanceof File) {
                                    File file = (File) value;
                                    multipartBuilder.addFormDataPart(entry.getKey(), file.getName(), RequestBody.create(MEDIA_TYPE, file));
                                    if (DEBUGMODE) {
                                        Log.i(">>>", "添加文件：" + entry.getKey() + ":" + file.getName());
                                    }
                                } else {
                                    multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue() + "");
                                }
                            }
                        } else {
                            multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue() + "");
                        }
                    }
                } else {
                    if (DEBUGMODE) {
                        Log.e(">>>", "-------------------------------------");
                        Log.e(">>>", "创建请求失败:无上传的文件");
                        Log.e(">>>", "=====================================");
                    }
                    return;
                }
                requestBody = multipartBuilder.build();
            } else if (isJsonRequest) {
                if (parameterInterceptListener != null) {
                    try {
                        if (jsonParameter.startsWith("[")) {
                            jsonParameter = parameterInterceptListener.onIntercept(context.get(), url, JsonList.parse(jsonParameter)).toString();
                        } else if (jsonParameter.startsWith("{")) {
                            jsonParameter = parameterInterceptListener.onIntercept(context.get(), url, JsonMap.parse(jsonParameter)).toString();
                        } else {
                            jsonParameter = (String) parameterInterceptListener.onIntercept(context.get(), url, jsonParameter);
                        }
                    } catch (Exception e) {
                    }
                }
                if (isNull(jsonParameter)) {
                    if (DEBUGMODE) {
                        Log.e(">>>", "-------------------------------------");
                        Log.e(">>>", "创建请求失败:" + jsonParameter + " 不是正确的json格式参数");
                        Log.e(">>>", "=====================================");
                    }
                    return;
                }
                requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParameter);
            } else if (isStringRequest) {
                if (parameterInterceptListener != null) {
                    try {
                        stringParameter = (String) parameterInterceptListener.onIntercept(context.get(), url, stringParameter);
                    } catch (Exception e) {
                    }
                }
                if (isNull(stringParameter)) {
                    if (DEBUGMODE) {
                        Log.e(">>>", "-------------------------------------");
                        Log.e(">>>", "创建请求失败:" + stringParameter);
                        Log.e(">>>", "=====================================");
                    }
                    return;
                }
                requestBody = RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), stringParameter);
            } else {
                if (parameterInterceptListener != null) {
                    try {
                        parameter = (Parameter) parameterInterceptListener.onIntercept(context.get(), url, parameter);
                    } catch (Exception e) {
                    }
                }
                requestBody = parameter.toOkHttpParameter();
            }
            
            //请求类型处理
            switch (requestType) {
                case GET_REQUEST:               //GET
                    builder.url(url.contains("?") ? url + "&" + parameter.toParameterString() : url + "?" + parameter.toParameterString());
                    break;
                case PUT_REQUEST:               //PUT
                    builder.url(url);
                    builder.put(requestBody);
                    break;
                case DELETE_REQUEST:            //DELETE
                    builder.url(url);
                    builder.delete(requestBody);
                    break;
                default:                        //POST
                    builder.url(url);
                    builder.post(requestBody);
                    break;
            }
            
            //请求头处理
            if (DEBUGMODE) {
                Log.i(">>>", "添加请求头:");
            }
            if (overallHeader != null && !overallHeader.entrySet().isEmpty()) {
                for (Map.Entry<String, Object> entry : overallHeader.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue() + "");
                    if (DEBUGMODE) {
                        Log.i(">>>>>>", entry.getKey() + "=" + entry.getValue());
                    }
                }
            }
            if (headers != null && !headers.entrySet().isEmpty()) {
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue() + "");
                    if (DEBUGMODE) {
                        Log.i(">>>>>>", entry.getKey() + "=" + entry.getValue());
                    }
                }
            }
            
            if (!isNull(cookieStr)) {
                builder.addHeader("Cookie", cookieStr);
            }
            request = builder.build();
            
            if (DEBUGMODE) {
                Log.i(">>>", "-------------------------------------");
                Log.i(">>>", "创建请求:" + url);
                Log.i(">>>", "参数:");
                if (isJsonRequest) {
                    if (!JsonFormat.formatJson(jsonParameter)) {
                        Log.i(">>>>>>", jsonParameter);
                    }
                } else if (isStringRequest) {
                    Log.i(">>>>>>", stringParameter);
                } else {
                    parameter.toPrintString();
                }
                Log.i(">>>", "请求已发送 ->");
            }
            
            isSending = true;
            checkTimeOut();
            httpCall = okHttpClient.newCall(request);
            httpCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    if (!isSending) {
                        return;
                    }
                    isSending = false;
                    if (BaseOkHttp.reserveServiceUrls != null && BaseOkHttp.reserveServiceUrls.length != 0) {
                        if (DEBUGMODE) {
                            Log.e(">>>", "服务器：" + BaseOkHttp.serviceUrl + "请求失败 ×");
                            if (reserveUrlIndex != BaseOkHttp.reserveServiceUrls.length) {
                                BaseOkHttp.serviceUrl = BaseOkHttp.reserveServiceUrls[reserveUrlIndex];
                                reserveUrlIndex++;
                                Log.e(">>>", "尝试更换为备用地址后重试：" + BaseOkHttp.serviceUrl);
                                send();
                            } else {
                                Log.e(">>>", "所有备用地址全部尝试完毕。请求失败 ×");
                            }
                            Log.e(">>>", "=====================================");
                        }
                    } else {
                        if (DEBUGMODE) {
                            Log.e(">>>", "请求失败:" + url);
                            Log.e(">>>", "参数:");
                            if (isJsonRequest) {
                                if (!JsonFormat.formatJson(jsonParameter, 1)) {
                                    Log.e(">>>>>>", jsonParameter);
                                }
                            } else if (isStringRequest) {
                                Log.i(">>>>>>", stringParameter);
                            } else {
                                parameter.toPrintString(1);
                            }
                            if (e != null) {
                                Log.e(">>>", "错误:" + e.toString());
                            } else {
                                Log.e(">>>", "请求发生错误: httpCall.onFailure & Exception is Null");
                            }
                            Log.e(">>>", "=====================================");
                        }
                        //回到主线程处理
                        runOnMain(new Runnable() {
                            @Override
                            public void run() {
                                if (responseInterceptListener != null) {
                                    if (responseInterceptListener.onResponse(context.get(), url, null, e)) {
                                        if (responseListener != null) {
                                            responseListener.onResponse(null, e);
                                        }
                                    }
                                } else {
                                    if (responseListener != null) {
                                        responseListener.onResponse(null, e);
                                    }
                                }
                            }
                        });
                    }
                }
                
                @Override
                public void onResponse(Call call, baseokhttp3.Response response) throws IOException {
                    if (!isSending) {
                        return;
                    }
                    isSending = false;
                    final String strResponse = response.body().string();
                    if (DEBUGMODE) {
                        Log.i(">>>", "请求成功:" + url);
                        Log.i(">>>", "参数:");
                        if (isJsonRequest) {
                            if (!JsonFormat.formatJson(jsonParameter)) {
                                Log.i(">>>>>>", jsonParameter);
                            }
                        } else if (isStringRequest) {
                            Log.i(">>>>>>", stringParameter);
                        } else {
                            parameter.toPrintString();
                        }
                        Log.i(">>>", "返回内容:");
                        if (!JsonFormat.formatJson(strResponse)) {
                            Log.i(">>>", strResponse);
                        }
                        Log.i(">>>", "=====================================");
                    }
                    
                    //回到主线程处理
                    runOnMain(new Runnable() {
                        @Override
                        public void run() {
                            if (responseInterceptListener != null) {
                                if (responseInterceptListener.onResponse(context.get(), url, strResponse, null)) {
                                    if (responseListener != null) {
                                        responseListener.onResponse(strResponse, null);
                                    }
                                }
                            } else {
                                if (responseListener != null) {
                                    responseListener.onResponse(strResponse, null);
                                }
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            if (DEBUGMODE) {
                Log.e(">>>", "请求创建失败:" + url);
                Log.e(">>>", "参数:");
                if (isJsonRequest) {
                    if (!JsonFormat.formatJson(jsonParameter, 1)) {
                        Log.e(">>>>>>", jsonParameter);
                    } else if (isStringRequest) {
                        Log.i(">>>>>>", stringParameter);
                    }
                } else {
                    if (parameter != null) {
                        parameter.toPrintString(1);
                    }
                }
                Log.e(">>>", "错误:" + e.toString());
                e.printStackTrace();
                Log.e(">>>", "=====================================");
            }
        }
    }
    
    private String getRealRequestUrl(String url) {
        String serviceUrl = BaseOkHttp.serviceUrl;
        if (serviceUrl.endsWith("/") && url.startsWith("/")) {
            return serviceUrl + url.substring(1);
        }
        if (!serviceUrl.endsWith("/") && !url.startsWith("/")) {
            return serviceUrl + "/" + url;
        }
        return serviceUrl + url;
    }
    
    private void download() {
        try {
            if (isNull(requestUrl)) {
                Log.e(">>>", "-------------------------------------");
                Log.e(">>>", "创建请求失败: 请求地址不能为空");
                Log.e(">>>", "=====================================");
            }
            if (!requestUrl.startsWith("http")) {
                url = getRealRequestUrl(requestUrl);
            } else {
                url = requestUrl;
            }
            
            if (!skipSSLCheck && SSLInAssetsFileName != null && !SSLInAssetsFileName.isEmpty()) {
                okHttpClient = getOkHttpClient(context.get(), context.get().getAssets().open(SSLInAssetsFileName));
            } else {
                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .retryOnConnectionFailure(false)
                        .connectTimeout(timeoutDuration, TimeUnit.SECONDS)
                        .writeTimeout(timeoutDuration, TimeUnit.SECONDS)
                        .readTimeout(timeoutDuration, TimeUnit.SECONDS)
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        });
                
                if (autoSaveCookies) {
                    builder.cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                            cookieStore.put(url, cookies);
                            if (DEBUGMODE) {
                                for (Cookie cookie : cookies) {
                                    Log.i("<<<", "saveCookie: " + cookie.name() + " path:" + cookie.path());
                                }
                            }
                        }
                        
                        @Override
                        public List<Cookie> loadForRequest(HttpUrl url) {
                            List<Cookie> cookies = cookieStore.get(url.host());
                            return cookies != null ? cookies : new ArrayList<Cookie>();
                        }
                    });
                }
                
                okHttpClient = builder.build();
            }
            
            //创建请求
            baseokhttp3.Request request;
            baseokhttp3.Request.Builder builder = new baseokhttp3.Request.Builder();
            
            builder.url(url);
            builder.addHeader("Connection", "close");
            request = builder.build();
            
            if (DEBUGMODE) {
                Log.i(">>>", "-------------------------------------");
                Log.i(">>>", "开始下载:" + url);
                Log.i(">>>", "=====================================");
            }
            httpCall = okHttpClient.newCall(request);
            httpCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    if (DEBUGMODE) {
                        Log.e(">>>", "-------------------------------------");
                        Log.e(">>>", "下载失败:" + e.getMessage());
                        e.printStackTrace();
                        Log.e(">>>", "=====================================");
                    }
                    runOnMain(new Runnable() {
                        @Override
                        public void run() {
                            onDownloadListener.onDownloadFailed(e);
                        }
                    });
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len = 0;
                    FileOutputStream fos = null;
                    
                    //储存下载文件的目录
                    File dir = downloadFile.getParentFile();
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    
                    try {
                        is = response.body().byteStream();
                        long total = response.body().contentLength();
                        fos = new FileOutputStream(downloadFile);
                        long sum = 0;
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            sum += len;
                            final int progress = (int) (sum * 1.0f / total * 100);
                            if (DEBUGMODE && DETAILSLOGS) {
                                Log.i(">>>", "下载中:" + progress);
                            }
                            runOnMain(new Runnable() {
                                @Override
                                public void run() {
                                    onDownloadListener.onDownloading(progress);
                                }
                            });
                        }
                        fos.flush();
                        //下载完成
                        if (DEBUGMODE) {
                            Log.i(">>>", "-------------------------------------");
                            Log.i(">>>", "下载完成:" + url);
                            Log.i(">>>", "存储文件:" + downloadFile.getAbsolutePath());
                            Log.i(">>>", "=====================================");
                        }
                        runOnMain(new Runnable() {
                            @Override
                            public void run() {
                                onDownloadListener.onDownloadSuccess(downloadFile);
                            }
                        });
                    } catch (final Exception e) {
                        if (DEBUGMODE) {
                            Log.e(">>>", "-------------------------------------");
                            Log.e(">>>", "下载过程错误:" + e.getMessage());
                            e.printStackTrace();
                            Log.e(">>>", "=====================================");
                        }
                        runOnMain(new Runnable() {
                            @Override
                            public void run() {
                                onDownloadListener.onDownloadFailed(e);
                            }
                        });
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {
                        
                        }
                    }
                }
            });
        } catch (Exception e) {
        
        }
    }
    
    private Timer timer;
    private static int reserveUrlIndex;
    
    private void checkTimeOut() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isSending) {
                    isSending = false;
                    if (BaseOkHttp.reserveServiceUrls != null && BaseOkHttp.reserveServiceUrls.length != 0) {
                        if (DEBUGMODE) {
                            Log.e(">>>", "服务器：" + BaseOkHttp.serviceUrl + "请求超时 ×");
                            if (reserveUrlIndex != BaseOkHttp.reserveServiceUrls.length) {
                                BaseOkHttp.serviceUrl = BaseOkHttp.reserveServiceUrls[reserveUrlIndex];
                                reserveUrlIndex++;
                                Log.e(">>>", "尝试更换为备用地址后重试：" + BaseOkHttp.serviceUrl);
                                send();
                            } else {
                                Log.e(">>>", "所有备用地址全部尝试完毕。请求超时 ×");
                            }
                            Log.e(">>>", "=====================================");
                        }
                    } else {
                        if (DEBUGMODE) {
                            Log.e(">>>", "请求超时 ×");
                            Log.e(">>>", "=====================================");
                        }
                        runOnMain(new Runnable() {
                            @Override
                            public void run() {
                                if (responseListener != null) {
                                    responseListener.onResponse(null, new TimeOutException());
                                }
                            }
                        });
                    }
                }
            }
        }, timeoutDuration * 1000);
    }
    
    private OkHttpClient getOkHttpClient(Context context, InputStream... certificates) {
        if (okHttpClient == null) {
            File sdcache = context.getExternalCacheDir();
            int cacheSize = 10 * 1024 * 1024;
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(false)
                    .connectTimeout(timeoutDuration, TimeUnit.SECONDS)
                    .writeTimeout(timeoutDuration, TimeUnit.SECONDS)
                    .readTimeout(timeoutDuration, TimeUnit.SECONDS)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            if (DEBUGMODE) {
                                Log.i("<<<", "hostnameVerifier: " + hostname);
                            }
                            if (httpsVerifyServiceUrl) {
                                if (serviceUrl.contains(hostname)) {
                                    return true;
                                } else {
                                    return false;
                                }
                            } else {
                                return true;
                            }
                        }
                    })
                    .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));
            if (certificates != null) {
                builder.sslSocketFactory(getSSLSocketFactory(certificates));
            }
            if (autoSaveCookies) {
                builder.cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url, cookies);
                        if (DEBUGMODE) {
                            for (Cookie cookie : cookies) {
                                Log.i("<<<", "saveCookie: " + cookie.name() + " path:" + cookie.path());
                            }
                        }
                    }
                    
                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                });
            }
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }
    
    private static SSLSocketFactory getSSLSocketFactory(InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                
                try {
                    if (certificate != null) {
                        certificate.close();
                    }
                } catch (IOException e) {
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private boolean isNull(String s) {
        if (s == null || s.trim().isEmpty() || "null".equals(s) || "(null)".equals(s)) {
            return true;
        }
        return false;
    }
    
    private HttpRequest() {
    }
    
    public static HttpRequest build(Context context, String url) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = new WeakReference<Context>(context);
            httpRequest.requestUrl = url;
            httpRequest.httpRequest = httpRequest;
            return httpRequest;
        }
    }
    
    public HttpRequest addParameter(String key, Object value) {
        if (parameter == null) {
            parameter = new Parameter();
        }
        parameter.add(key, value);
        this.jsonParameter = null;
        this.stringParameter = null;
        return this;
    }
    
    public HttpRequest setParameter(Parameter parameter) {
        this.parameter = parameter;
        this.jsonParameter = null;
        this.stringParameter = null;
        return this;
    }
    
    public HttpRequest setStringParameter(String stringParameter) {
        this.stringParameter = stringParameter;
        this.parameter = null;
        return this;
    }
    
    public HttpRequest setJsonParameter(String jsonParameter) {
        this.jsonParameter = jsonParameter;
        return this;
    }
    
    public HttpRequest setJsonParameter(JsonMap jsonParameter) {
        if (jsonParameter == null) {
            this.jsonParameter = null;
        } else {
            this.jsonParameter = jsonParameter.toString();
        }
        return this;
    }
    
    public HttpRequest setJsonParameter(JsonList jsonParameter) {
        if (jsonParameter == null) {
            this.jsonParameter = null;
        } else {
            this.jsonParameter = jsonParameter.toString();
        }
        return this;
    }
    
    public HttpRequest addHeaders(String key, String value) {
        if (headers == null) {
            headers = new Parameter();
        }
        headers.add(key, value);
        return this;
    }
    
    public HttpRequest setHeaders(Parameter headers) {
        this.headers = headers;
        return this;
    }
    
    public HashMap<HttpUrl, List<Cookie>> getCookies() {
        return cookieStore;
    }
    
    public HttpRequest cleanCookies() {
        this.cookieStore = new HashMap<>();
        return this;
    }
    
    public HttpRequest setUrl(String url) {
        this.requestUrl = url;
        return this;
    }
    
    public HttpRequest setResponseListener(ResponseListener listener) {
        this.responseListener = listener;
        return this;
    }
    
    public HttpRequest setJsonResponseListener(JsonResponseListener jsonResponseListener) {
        this.responseListener = jsonResponseListener;
        return this;
    }
    
    public void doPost() {
        requestType = POST_REQUEST;
        send();
    }
    
    public void doGet() {
        requestType = GET_REQUEST;
        send();
    }
    
    public void doDelete() {
        requestType = DELETE_REQUEST;
        send();
    }
    
    public void doPut() {
        requestType = PUT_REQUEST;
        send();
    }
    
    private File downloadFile;
    private OnDownloadListener onDownloadListener;
    
    public void doDownload(File downloadFile, OnDownloadListener onDownloadListener) {
        requestType = DOWNLOAD;
        this.downloadFile = downloadFile;
        this.onDownloadListener = onDownloadListener;
        download();
    }
    
    public String getCookie() {
        return cookieStr;
    }
    
    public HttpRequest setCookie(String cookie) {
        this.cookieStr = cookieStr;
        return this;
    }
    
    public HttpRequest setMediaType(MediaType mediaType) {
        MEDIA_TYPE = mediaType;
        return this;
    }
    
    public HttpRequest skipSSLCheck() {
        skipSSLCheck = true;
        return this;
    }
    
    public File getDownloadFile() {
        return downloadFile;
    }
    
    public void stop() {
        if (httpCall != null) {
            httpCall.cancel();
        }
    }
    
    private void runOnMain(Runnable runnable) {
        if (context == null || context.get() == null) {
            stop();
            return;
        }
        if (context.get() instanceof Activity) {
            if (((Activity) context.get()).isFinishing()) {
                stop();
                return;
            }
            ((Activity) context.get()).runOnUiThread(runnable);
        } else {
            if (DEBUGMODE && DETAILSLOGS) {
                Log.i(">>>", "context 不是 Activity，本次请求在异步线程返回 >>>");
            }
            runnable.run();
        }
    }
    
    public void onDetach() {
        context.clear();
    }
    
    public int getTimeoutDuration() {
        return timeoutDuration;
    }
    
    public HttpRequest setTimeoutDuration(int timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }
}
