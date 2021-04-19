package com.kongzue.baseokhttp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.kongzue.baseokhttp.exceptions.DecodeJsonException;
import com.kongzue.baseokhttp.exceptions.TimeOutException;
import com.kongzue.baseokhttp.listener.BaseResponseListener;
import com.kongzue.baseokhttp.listener.CustomOkHttpClient;
import com.kongzue.baseokhttp.listener.CustomOkHttpClientBuilder;
import com.kongzue.baseokhttp.listener.JsonResponseListener;
import com.kongzue.baseokhttp.listener.MultipartBuilderInterceptor;
import com.kongzue.baseokhttp.listener.OnDownloadListener;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.baseokhttp.listener.UploadProgressListener;
import com.kongzue.baseokhttp.util.BaseOkHttp;
import com.kongzue.baseokhttp.util.JsonFormat;
import com.kongzue.baseokhttp.util.JsonList;
import com.kongzue.baseokhttp.util.JsonMap;
import com.kongzue.baseokhttp.util.LockLog;
import com.kongzue.baseokhttp.util.Parameter;
import com.kongzue.baseokhttp.util.RequestBodyImpl;
import com.kongzue.baseokhttp.util.RequestInfo;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.Proxy;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    
    private String customMimeType;
    
    private Parameter parameter;
    private Parameter headers;
    private WeakReference<Context> context;
    private HttpRequest httpRequest;
    private BaseResponseListener responseListener;
    private String requestUrl;
    private String jsonParameter;
    private String stringParameter;
    private int timeoutDuration = TIME_OUT_DURATION;
    private Proxy proxy;
    private UploadProgressListener uploadProgressListener;
    
    private CustomOkHttpClient customOkHttpClient;
    private CustomOkHttpClientBuilder customOkHttpClientBuilder;
    
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
    
    public static void JSONPOST(Context context, String url, JsonMap jsonMap, BaseResponseListener listener) {
        JSONPOST(context, url, null, jsonMap.toString(), listener);
    }
    
    public static void JSONPOST(Context context, String url, JSONObject jsonObject, BaseResponseListener listener) {
        JSONPOST(context, url, null, jsonObject.toString(), listener);
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
    
    public static void JSONPOST(Context context, String url, Parameter headers, JsonMap jsonMap, BaseResponseListener listener) {
        JSONPOST(context, url, headers, jsonMap.toString(), listener);
    }
    
    public static void JSONPOST(Context context, String url, Parameter headers, JSONObject jsonObject, BaseResponseListener listener) {
        JSONPOST(context, url, headers, jsonObject.toString(), listener);
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
    
    private RequestInfo requestInfo;
    
    private void send() {
        timeoutDuration = TIME_OUT_DURATION;
        isFileRequest = false;
        isJsonRequest = false;
        isStringRequest = false;
        if (proxy == null) {
            proxy = BaseOkHttp.proxy;
        }
        
        if (parameter != null && !parameter.entrySet().isEmpty()) {
            for (Map.Entry<String, Object> entry : parameter.entrySet()) {
                if (entry.getValue() instanceof File) {
                    isFileRequest = true;
                    break;
                } else if (entry.getValue() instanceof List) {
                    List valueList = (List) entry.getValue();
                    for (Object value : valueList) {
                        if (value instanceof File) {
                            isFileRequest = true;
                            break;
                        }
                    }
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
                LockLog.Builder.create()
                        .e(">>>", "-------------------------------------")
                        .e(">>>", "创建请求失败: 请求地址不能为空")
                        .e(">>>", "=====================================")
                        .build();
                return;
            }
            
            //全局参数
            if (overallParameter != null && !overallParameter.entrySet().isEmpty()) {
                for (Map.Entry<String, Object> entry : overallParameter.entrySet()) {
                    parameter.add(entry.getKey(), entry.getValue());
                }
            }
    
            if (BaseOkHttp.globalCustomOkHttpClient!=null){
                BaseOkHttp.globalCustomOkHttpClient.customBuilder(this,okHttpClient);
            }
            if (customOkHttpClient != null) {
                okHttpClient = customOkHttpClient.customBuilder(okHttpClient);
            }else{
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
                    if (proxy != null) {
                        builder.proxy(proxy);
                    }
                    if (customOkHttpClientBuilder != null) {
                        builder = customOkHttpClientBuilder.customBuilder(builder);
                    }
                    if (BaseOkHttp.globalCustomOkHttpClientBuilder != null) {
                        builder = BaseOkHttp.globalCustomOkHttpClientBuilder.customBuilder(this,builder);
                    }
                    okHttpClient = builder.build();
                }
            }
            
            //创建请求
            Request request;
            Request.Builder builder = new Request.Builder();
            
            RequestBodyImpl requestBody = null;
            
            if (isFileRequest) {
                requestInfo = new RequestInfo(url, parameter);
                if (disallowSameRequest && equalsRequestInfo(requestInfo)) {
                    return;
                }
                addRequestInfo(requestInfo);
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
                            multipartBuilder.addFormDataPart(entry.getKey(), file.getName(), RequestBody.create(MediaType.parse(getMimeType(file)), file));
                            if (DEBUGMODE) {
                                LockLog.logI(">>>", "添加文件：" + entry.getKey() + ":" + file.getName());
                            }
                        } else if (entry.getValue() instanceof List) {
                            List valueList = (List) entry.getValue();
                            for (Object value : valueList) {
                                if (value instanceof File) {
                                    File file = (File) value;
                                    multipartBuilder.addFormDataPart(entry.getKey(), file.getName(), RequestBody.create(MediaType.parse(getMimeType(file)), file));
                                    if (DEBUGMODE) {
                                        LockLog.logI(">>>", "添加文件：" + entry.getKey() + ":" + file.getName());
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
                        LockLog.Builder.create()
                                .e(">>>", "-------------------------------------")
                                .e(">>>", "创建请求失败:无上传的文件")
                                .e(">>>", "=====================================")
                                .build();
                    }
                    return;
                }
                multipartBuilder = interceptMultipartBuilder(multipartBuilder);
                requestBody = new RequestBodyImpl(multipartBuilder.build()) {
                    @Override
                    public void loading(long current, long total, boolean done) {
                        uploadProgressCallback(current, total, done);
                    }
                };
            } else if (isJsonRequest) {
                requestInfo = new RequestInfo(url, jsonParameter);
                if (disallowSameRequest && equalsRequestInfo(requestInfo)) {
                    return;
                }
                addRequestInfo(requestInfo);
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
                        LockLog.Builder.create()
                                .e(">>>", "-------------------------------------")
                                .e(">>>", "创建请求失败:" + jsonParameter + " 不是正确的json格式参数")
                                .e(">>>", "=====================================")
                                .build();
                    }
                    return;
                }
                requestBody = new RequestBodyImpl(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParameter)) {
                    @Override
                    public void loading(long current, long total, boolean done) {
                        uploadProgressCallback(current, total, done);
                    }
                };
            } else if (isStringRequest) {
                requestInfo = new RequestInfo(url, stringParameter);
                if (disallowSameRequest && equalsRequestInfo(requestInfo)) {
                    return;
                }
                addRequestInfo(requestInfo);
                if (parameterInterceptListener != null) {
                    try {
                        stringParameter = (String) parameterInterceptListener.onIntercept(context.get(), url, stringParameter);
                    } catch (Exception e) {
                    }
                }
                if (isNull(stringParameter)) {
                    if (DEBUGMODE) {
                        LockLog.Builder.create()
                                .e(">>>", "-------------------------------------")
                                .e(">>>", "创建请求失败:" + stringParameter)
                                .e(">>>", "=====================================")
                                .build();
                    }
                    return;
                }
                requestBody = new RequestBodyImpl(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), stringParameter)) {
                    @Override
                    public void loading(long current, long total, boolean done) {
                        uploadProgressCallback(current, total, done);
                    }
                };
            } else {
                requestInfo = new RequestInfo(url, parameter);
                if (disallowSameRequest && equalsRequestInfo(requestInfo)) {
                    return;
                }
                addRequestInfo(requestInfo);
                if (parameterInterceptListener != null) {
                    try {
                        parameter = (Parameter) parameterInterceptListener.onIntercept(context.get(), url, parameter);
                    } catch (Exception e) {
                    }
                }
                requestBody = new RequestBodyImpl(parameter.toOkHttpParameter()) {
                    @Override
                    public void loading(long current, long total, boolean done) {
                        uploadProgressCallback(current, total, done);
                    }
                };
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
                LockLog.logI(">>>", "添加请求头:");
            }
            Parameter allHeader = new Parameter();
            if (overallHeader != null && !overallHeader.entrySet().isEmpty()) {
                allHeader.putAll(overallHeader);
            }
            if (headers != null && !headers.entrySet().isEmpty()) {
                allHeader.putAll(headers);
            }
            if (headerInterceptListener != null) {
                allHeader = headerInterceptListener.onIntercept(context.get(), url, allHeader);
            }
            for (Map.Entry<String, Object> entry : allHeader.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue() + "");
                if (DEBUGMODE) {
                    LockLog.logI(">>>>>>", entry.getKey() + "=" + entry.getValue());
                }
            }
            if (!isNull(cookieStr)) {
                builder.addHeader("Cookie", cookieStr);
            }
            request = builder.build();
            
            if (DEBUGMODE) {
                LockLog.Builder logBuilder = LockLog.Builder.create()
                        .i(">>>", "-------------------------------------")
                        .i(">>>", "创建请求:" + url)
                        .i(">>>", "参数:");
                
                if (isJsonRequest) {
                    List<LockLog.LogBody> jsonLogList = JsonFormat.formatJson(jsonParameter);
                    if (jsonLogList == null) {
                        logBuilder.i(">>>>>>", jsonParameter);
                    } else {
                        logBuilder.add(jsonLogList);
                    }
                } else if (isStringRequest) {
                    logBuilder.i(">>>>>>", stringParameter);
                } else {
                    logBuilder.i(">>>>>>", parameter.toParameterString());
                }
                logBuilder.i(">>>", "请求已发送 ->")
                        .build();
            }
            
            isSending = true;
            checkTimeOut();
            httpCall = okHttpClient.newCall(request);
            httpCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    deleteRequestInfo(requestInfo);
                    if (!isSending) {
                        return;
                    }
                    isSending = false;
                    if (BaseOkHttp.reserveServiceUrls != null && BaseOkHttp.reserveServiceUrls.length != 0) {
                        if (DEBUGMODE) {
                            LockLog.Builder logBuilder = LockLog.Builder.create()
                                    .e(">>>", "服务器：" + BaseOkHttp.serviceUrl + "请求失败 ×");
                            if (reserveUrlIndex != BaseOkHttp.reserveServiceUrls.length) {
                                BaseOkHttp.serviceUrl = BaseOkHttp.reserveServiceUrls[reserveUrlIndex];
                                reserveUrlIndex++;
                                
                                logBuilder.e(">>>", "尝试更换为备用地址后重试：" + BaseOkHttp.serviceUrl);
                                send();
                            } else {
                                logBuilder.e(">>>", "所有备用地址全部尝试完毕。请求失败 ×");
                            }
                            logBuilder.e(">>>", "=====================================")
                                    .build();
                        }
                    } else {
                        if (DEBUGMODE) {
                            LockLog.Builder logBuilder = LockLog.Builder.create()
                                    .e(">>>", "请求失败:" + url)
                                    .e(">>>", "参数:");
                            if (isJsonRequest) {
                                List<LockLog.LogBody> jsonLogList = JsonFormat.formatJson(jsonParameter);
                                if (jsonLogList == null) {
                                    logBuilder.e(">>>>>>", jsonParameter);
                                } else {
                                    logBuilder.add(jsonLogList);
                                }
                            } else if (isStringRequest) {
                                logBuilder.e(">>>>>>", stringParameter);
                            } else {
                                logBuilder.e(">>>>>>", parameter.toParameterString());
                            }
                            if (e != null) {
                                logBuilder.e(">>>", "错误:" + LockLog.getExceptionInfo(e));
                            } else {
                                logBuilder.e(">>>", "请求发生错误: httpCall.onFailure & Exception is Null");
                            }
                            logBuilder.e(">>>", "=====================================")
                                    .build();
                        }
                        //回到主线程处理
                        runOnMain(new Runnable() {
                            @Override
                            public void run() {
                                if (responseInterceptListener != null) {
                                    if (responseInterceptListener.response(context.get(), url, null, e)) {
                                        if (responseListener != null) {
                                            responseListener.response(null, e);
                                        }
                                    }
                                } else {
                                    if (responseListener != null) {
                                        responseListener.response(null, e);
                                    }
                                }
                            }
                        });
                    }
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    deleteRequestInfo(requestInfo);
                    if (!isSending) {
                        return;
                    }
                    isSending = false;
                    final String strResponse = response.body().string();
                    if (DEBUGMODE) {
                        LockLog.Builder logBuilder = LockLog.Builder.create()
                                .i(">>>", "请求成功:" + url)
                                .i(">>>", "参数:");
                        if (isJsonRequest) {
                            List<LockLog.LogBody> jsonLogList = JsonFormat.formatJson(jsonParameter);
                            if (jsonLogList == null) {
                                logBuilder.i(">>>>>>", jsonParameter);
                            } else {
                                logBuilder.add(jsonLogList);
                            }
                        } else if (isStringRequest) {
                            logBuilder.i(">>>>>>", stringParameter);
                        } else {
                            logBuilder.i(">>>>>>", parameter.toParameterString());
                        }
                        logBuilder.i(">>>", "返回内容:");
                        List<LockLog.LogBody> jsonLogList = JsonFormat.formatJson(strResponse);
                        if (jsonLogList == null) {
                            logBuilder.i(">>>", strResponse);
                        } else {
                            logBuilder.add(jsonLogList);
                        }
                        logBuilder.i(">>>", "=====================================")
                                .build();
                    }
                    
                    //回到主线程处理
                    runOnMain(new Runnable() {
                        @Override
                        public void run() {
                            if (responseInterceptListener != null) {
                                if (responseInterceptListener.response(context.get(), url, strResponse, null)) {
                                    if (responseListener != null) {
                                        responseListener.response(strResponse, null);
                                    }
                                }
                            } else {
                                if (responseListener != null) {
                                    responseListener.response(strResponse, null);
                                }
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            deleteRequestInfo(requestInfo);
            if (DEBUGMODE) {
                LockLog.Builder logBuilder = LockLog.Builder.create()
                        .e(">>>", "请求创建失败:" + url)
                        .e(">>>", "参数:");
                if (isJsonRequest) {
                    if (isStringRequest) {
                        logBuilder.e(">>>>>>", stringParameter);
                    } else {
                        List<LockLog.LogBody> jsonLogList = JsonFormat.formatJson(jsonParameter);
                        if (jsonLogList == null) {
                            logBuilder.e(">>>>>>", jsonParameter);
                        } else {
                            logBuilder.add(jsonLogList);
                        }
                    }
                } else {
                    if (parameter != null) {
                        logBuilder.e(">>>>>>", parameter.toParameterString());
                    }
                }
                logBuilder.e(">>>", "错误:" + LockLog.getExceptionInfo(e));
                logBuilder.e(">>>", "=====================================");
                logBuilder.build();
            }
        }
    }
    
    private void uploadProgressCallback(final long current, final long total, final boolean done) {
        runOnMain(new Runnable() {
            @Override
            public void run() {
                if (uploadProgressListener != null) {
                    uploadProgressListener.onUpload(total != 0 ? current * 1.0f / total : 0f, current, total, done);
                }
            }
        });
    }
    
    public String getMimeType(File file) {
        if (!isNull(customMimeType)) {
            return customMimeType;
        }
        String suffix = getSuffix(file);
        if (suffix == null) {
            return "file/*";
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (!isNull(type)) {
            return type;
        }
        return "file/*";
    }
    
    private static String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
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
    
    private int oldDownloadProgress = -1;
    
    private void download() {
        if (proxy == null) {
            proxy = BaseOkHttp.proxy;
        }
        try {
            oldDownloadProgress = -1;
            if (isNull(requestUrl)) {
                LockLog.Builder.create()
                        .e(">>>", "-------------------------------------")
                        .e(">>>", "创建请求失败: 请求地址不能为空")
                        .e(">>>", "=====================================")
                        .build();
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
                
                if (proxy != null) {
                    builder.proxy(proxy);
                }
                if (autoSaveCookies) {
                    builder.cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                            cookieStore.put(url, cookies);
                            if (DEBUGMODE) {
                                for (Cookie cookie : cookies) {
                                    LockLog.logI("<<<", "saveCookie: " + cookie.name() + " path:" + cookie.path());
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
                
                if (customOkHttpClientBuilder != null) {
                    builder = customOkHttpClientBuilder.customBuilder(builder);
                }
                if (BaseOkHttp.globalCustomOkHttpClientBuilder != null) {
                    builder = BaseOkHttp.globalCustomOkHttpClientBuilder.customBuilder(this,builder);
                }
                okHttpClient = builder.build();
            }
            if (customOkHttpClient != null) {
                okHttpClient = customOkHttpClient.customBuilder(okHttpClient);
            }
            if (BaseOkHttp.globalCustomOkHttpClient != null) {
                okHttpClient = BaseOkHttp.globalCustomOkHttpClient.customBuilder(this, okHttpClient);
            }
            
            //创建请求
            Request request;
            Request.Builder builder = new Request.Builder();
            builder.url(url);
            builder.addHeader("Connection", "close");
            request = builder.build();
            
            if (DEBUGMODE) {
                LockLog.Builder.create()
                        .i(">>>", "-------------------------------------")
                        .i(">>>", "开始下载:" + url)
                        .i(">>>", "=====================================")
                        .build();
            }
            httpCall = okHttpClient.newCall(request);
            httpCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    if (DEBUGMODE) {
                        LockLog.Builder.create()
                                .e(">>>", "-------------------------------------")
                                .e(">>>", "下载失败:" + e.getMessage())
                                .e(">>>", "=====================================")
                                .build();
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
                                if (oldDownloadProgress != progress) {
                                    LockLog.logI(">>>", "下载中:" + progress);
                                    oldDownloadProgress = progress;
                                }
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
                            LockLog.Builder.create()
                                    .i(">>>", "-------------------------------------")
                                    .i(">>>", "下载完成:" + url)
                                    .i(">>>", "存储文件:" + downloadFile.getAbsolutePath())
                                    .i(">>>", "=====================================")
                                    .build();
                        }
                        runOnMain(new Runnable() {
                            @Override
                            public void run() {
                                onDownloadListener.onDownloadSuccess(downloadFile);
                            }
                        });
                    } catch (final Exception e) {
                        if (DEBUGMODE) {
                            LockLog.Builder.create()
                                    .e(">>>", "-------------------------------------")
                                    .e(">>>", "下载过程错误:" + e.getMessage())
                                    .e(">>>", "=====================================")
                                    .build();
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
                    deleteRequestInfo(requestInfo);
                    isSending = false;
                    if (BaseOkHttp.reserveServiceUrls != null && BaseOkHttp.reserveServiceUrls.length != 0) {
                        if (DEBUGMODE) {
                            LockLog.Builder logBuilder = LockLog.Builder.create()
                                    .e(">>>", "服务器：" + BaseOkHttp.serviceUrl + "请求超时 ×");
                            if (reserveUrlIndex != BaseOkHttp.reserveServiceUrls.length) {
                                BaseOkHttp.serviceUrl = BaseOkHttp.reserveServiceUrls[reserveUrlIndex];
                                reserveUrlIndex++;
                                logBuilder.e(">>>", "尝试更换为备用地址后重试：" + BaseOkHttp.serviceUrl);
                                send();
                            } else {
                                logBuilder.e(">>>", "所有备用地址全部尝试完毕。请求超时 ×");
                            }
                            logBuilder.e(">>>", "=====================================");
                            logBuilder.build();
                        }
                    } else {
                        if (DEBUGMODE) {
                            LockLog.Builder.create()
                                    .e(">>>", "请求超时 ×")
                                    .e(">>>", "=====================================")
                                    .build();
                        }
                        runOnMain(new Runnable() {
                            @Override
                            public void run() {
                                if (responseInterceptListener == null) {
                                    if (responseListener != null) {
                                        responseListener.response(null, new TimeOutException());
                                    }
                                } else {
                                    if (responseInterceptListener.response(context.get(), url, null, new TimeOutException())) {
                                        if (responseListener != null) {
                                            responseListener.response(null, new TimeOutException());
                                        }
                                    }
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
                                LockLog.logI("<<<", "hostnameVerifier: " + hostname);
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
                    .cache(BaseOkHttp.requestCache ? new Cache(sdcache.getAbsoluteFile(), cacheSize) : null);
            if (certificates != null) {
                builder.sslSocketFactory(getSSLSocketFactory(certificates));
            }
            if (proxy != null) {
                builder.proxy(proxy);
            }
            if (autoSaveCookies) {
                builder.cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url, cookies);
                        if (DEBUGMODE) {
                            for (Cookie cookie : cookies) {
                                LockLog.logI("<<<", "saveCookie: " + cookie.name() + " path:" + cookie.path());
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
            if (customOkHttpClientBuilder != null) {
                builder = customOkHttpClientBuilder.customBuilder(builder);
            }
            if (BaseOkHttp.globalCustomOkHttpClientBuilder != null) {
                builder = BaseOkHttp.globalCustomOkHttpClientBuilder.customBuilder(this,builder);
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
    
    public Proxy getProxy() {
        return proxy;
    }
    
    public HttpRequest setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
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
    
    @Deprecated
    public HttpRequest setMediaType(MediaType mediaType) {
        return this;
    }
    
    public HttpRequest setCustomMimeType(String customMimeType) {
        this.customMimeType = customMimeType;
        return this;
    }
    
    public String getCustomMimeType() {
        return customMimeType;
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
                LockLog.logI(">>>", "context 不是 Activity，本次请求在异步线程返回 >>>");
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
    
    public UploadProgressListener getUploadProgressListener() {
        return uploadProgressListener;
    }
    
    public HttpRequest setUploadProgressListener(UploadProgressListener uploadProgressListener) {
        this.uploadProgressListener = uploadProgressListener;
        return this;
    }
    
    private MultipartBuilderInterceptor multipartBuilderInterceptor;
    
    protected MultipartBody.Builder interceptMultipartBuilder(MultipartBody.Builder multipartBuilder) {
        if (multipartBuilderInterceptor != null) {
            multipartBuilder = multipartBuilderInterceptor.interceptMultipartBuilder(multipartBuilder);
        }
        return multipartBuilder;
    }
    
    public MultipartBuilderInterceptor getMultipartBuilderInterceptor() {
        return multipartBuilderInterceptor;
    }
    
    public HttpRequest setMultipartBuilderInterceptor(MultipartBuilderInterceptor multipartBuilderInterceptor) {
        this.multipartBuilderInterceptor = multipartBuilderInterceptor;
        return this;
    }
    
    public CustomOkHttpClient getCustomOkHttpClient() {
        return customOkHttpClient;
    }
    
    /**
     * 此方法用于请求前修改发出的 OkHttpClient，请将修改后的 OkHttpClient return 到此接口中
     * 警告：要使此方法生效，请使用 build(...) 方法构建 HttpRequest
     */
    public HttpRequest setCustomOkHttpClient(CustomOkHttpClient customOkHttpClient) {
        this.customOkHttpClient = customOkHttpClient;
        return this;
    }
    
    public CustomOkHttpClientBuilder getCustomOkHttpClientBuilder() {
        return customOkHttpClientBuilder;
    }
    
    /**
     * 此方法用于请求前修改发出的 OkHttpClientBuilder，请将修改后的 OkHttpClientBuilder return 到此接口中
     * 警告：要使此方法生效，请使用 build(...) 方法构建 HttpRequest
     */
    public HttpRequest setCustomOkHttpClientBuilder(CustomOkHttpClientBuilder customOkHttpClientBuilder) {
        this.customOkHttpClientBuilder = customOkHttpClientBuilder;
        return this;
    }
    
    public Parameter getParameter() {
        return parameter;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getJsonParameter() {
        return jsonParameter;
    }
    
    public String getStringParameter() {
        return stringParameter;
    }
    
    public boolean isFileRequest() {
        return isFileRequest;
    }
    
    public boolean isJsonRequest() {
        return isJsonRequest;
    }
    
    public boolean isStringRequest() {
        return isStringRequest;
    }
}
