package com.kongzue.baseokhttp;

import android.app.Activity;
import android.content.Context;
import android.webkit.MimeTypeMap;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.kongzue.baseokhttp.exceptions.SameRequestException;
import com.kongzue.baseokhttp.exceptions.TimeOutException;
import com.kongzue.baseokhttp.listener.BaseResponseListener;
import com.kongzue.baseokhttp.listener.CustomMimeInterceptor;
import com.kongzue.baseokhttp.listener.CustomOkHttpClient;
import com.kongzue.baseokhttp.listener.CustomOkHttpClientBuilder;
import com.kongzue.baseokhttp.listener.JsonResponseListener;
import com.kongzue.baseokhttp.listener.MultipartBuilderInterceptor;
import com.kongzue.baseokhttp.listener.OnDownloadListener;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.baseokhttp.listener.UploadProgressListener;
import com.kongzue.baseokhttp.util.BaseOkHttp;
import com.kongzue.baseokhttp.util.HttpEventListener;
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
import java.util.Objects;
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

    //自定义上传文件的 MIME 类型
    @Deprecated
    private String customMimeType;
    private CustomMimeInterceptor customMimeInterceptor;
    private Boolean async = null;

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
    private boolean showLog = true;

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

    public static void PATCH(Context context, String url, Parameter parameter, BaseResponseListener listener) {
        PATCH(context, url, null, parameter, listener);
    }

    public static void PATCH(Context context, String url, Parameter headers, Parameter parameter, BaseResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = new WeakReference<Context>(context);
            httpRequest.headers = headers;
            httpRequest.responseListener = listener;
            httpRequest.parameter = parameter;
            httpRequest.requestUrl = url;
            httpRequest.requestType = PATCH_REQUEST;
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

        if (skipShowLogUrl != null) {
            for (String element : skipShowLogUrl) {
                if (Objects.equals(element, requestUrl)) {
                    setShowLog(true);
                }
            }
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
            if (isNull(url) && isShowLog() && DEBUGMODE) {
                logError("创建请求失败: 请求地址不能为空");
                return;
            }

            // 全局参数
            if (overallParameter != null && !overallParameter.entrySet().isEmpty()) {
                for (Map.Entry<String, Object> entry : overallParameter.entrySet()) {
                    parameter.add(entry.getKey(), entry.getValue());
                }
            }

            okHttpClient = createClient();
            if (okHttpClient == null) {
                return;
            }

            Request request = createRequest();
            if (request == null) {
                return;
            }

            logRequestDetails(request);

            isSending = true;
            checkTimeOut();
            httpCall = okHttpClient.newCall(request);

            if (isAsync()) {
                try {
                    Response response = httpCall.execute();
                    onFinish(response);
                } catch (Exception e) {
                    onFail(e);
                }
            } else {
                httpCall.enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        onFinish(response);
                    }

                    @Override
                    public void onFailure(Call call, final IOException e) {
                        onFail(e);
                    }
                });
            }
        } catch (Exception e) {
            onFail(e);
        }
    }

    private void logError(String s) {
        LockLog.Builder.create()
                .e(">>>", "-------------------------------------")
                .e(">>>", s)
                .e(">>>", "=====================================")
                .build();
    }

    private void logRequestDetails(Request request) {
        if (DEBUGMODE && isShowLog()) {
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
    }

    /**
     * 请求异常处理
     *
     * @param e 异常
     */
    private void onFail(Exception e) {
        deleteRequestInfo(requestInfo);
        if (!isSending) {
            return;
        }
        isSending = false;
        if (BaseOkHttp.reserveServiceUrls != null && BaseOkHttp.reserveServiceUrls.length != 0) {
            if (DEBUGMODE && isShowLog()) {
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
            if (DEBUGMODE && isShowLog()) {
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

    /**
     * 请求完成处理
     *
     * @param response 返回信息
     */
    private void onFinish(Response response) {
        deleteRequestInfo(requestInfo);
        if (!isSending) {
            return;
        }
        isSending = false;
        final String strResponse;
        try {
            strResponse = response.body().string();
            if (DEBUGMODE && isShowLog()) {
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
                            for (ResponseListener listener : requestInfo.getSameRequestCallbacks()) {
                                if (listener != null) {
                                    listener.response(strResponse, new SameRequestException("发生重复请求: " + requestInfo));
                                }
                            }
                        }
                    } else {
                        if (responseListener != null) {
                            responseListener.response(strResponse, null);
                        }
                        for (ResponseListener listener : requestInfo.getSameRequestCallbacks()) {
                            if (listener != null) {
                                listener.response(strResponse, new SameRequestException("发生重复请求: " + requestInfo));
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            if (DEBUGMODE) e.printStackTrace();
            onFail(e);
        }
    }

    private OkHttpClient createClient() {
        if (BaseOkHttp.globalCustomOkHttpClient != null) {
            return BaseOkHttp.globalCustomOkHttpClient.customBuilder(this, okHttpClient);
        }
        if (customOkHttpClient != null) {
            return customOkHttpClient.customBuilder(okHttpClient);
        } else {
            if (!skipSSLCheck && !isNull(SSLInAssetsFileName)) {
                File sdCache = context.get().getExternalCacheDir();
                InputStream certificates = null;
                try {
                    certificates = context.get().getAssets().open(SSLInAssetsFileName);
                } catch (IOException e) {
                    if (DEBUGMODE && isShowLog()) {
                        LockLog.Builder logBuilder = LockLog.Builder.create();
                        logBuilder.e(">>>", "读取SSL证书错误:" + LockLog.getExceptionInfo(e));
                        logBuilder.e(">>>", "=====================================");
                        logBuilder.build();
                    }
                    return null;
                }
                int cacheSize = 10 * 1024 * 1024;
                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .retryOnConnectionFailure(false)
                        .connectTimeout(timeoutDuration, TimeUnit.SECONDS)
                        .writeTimeout(timeoutDuration, TimeUnit.SECONDS)
                        .readTimeout(timeoutDuration, TimeUnit.SECONDS)
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                if (DEBUGMODE && isShowLog()) {
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
                        .cache(BaseOkHttp.requestCache ? new Cache(sdCache.getAbsoluteFile(), cacheSize) : null);
                if (certificates != null) {
                    builder.sslSocketFactory(getSSLSocketFactory(certificates));
                }
                if (showTimeStamp) {
                    builder.eventListenerFactory(HttpEventListener.FACTORY);
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
                                    if (DEBUGMODE && isShowLog()) {
                                        LockLog.logI("<<<", "saveCookie: " + cookie.name() + " path:" + cookie.path());
                                    }
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
                if (BaseOkHttp.disableOriginInterceptors) {
                    builder.interceptors().clear();
                    builder.networkInterceptors().clear();
                }
                return builder.build();
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
                if (showTimeStamp) {
                    builder.eventListenerFactory(HttpEventListener.FACTORY);
                }
                if (customOkHttpClientBuilder != null) {
                    builder = customOkHttpClientBuilder.customBuilder(builder);
                }
                if (BaseOkHttp.globalCustomOkHttpClientBuilder != null) {
                    builder = BaseOkHttp.globalCustomOkHttpClientBuilder.customBuilder(this, builder);
                }
                if (BaseOkHttp.disableOriginInterceptors) {
                    builder.interceptors().clear();
                    builder.networkInterceptors().clear();
                }
                return builder.build();
            }
        }
    }

    private Request createRequest() {
        Request.Builder builder = new Request.Builder();

        RequestBody requestBody = null;

        if (isFileRequest) {
            requestInfo = new RequestInfo(url, parameter, context.get().hashCode());
            RequestInfo sameRequestInfo = equalsRequestInfo(requestInfo);
            if (disallowSameRequest && sameRequestInfo != null) {
                return null;
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
                        if (DEBUGMODE && isShowLog()) {
                            LockLog.logI(">>>", "添加文件：" + entry.getKey() + ":" + file.getName());
                        }
                    } else if (entry.getValue() instanceof List) {
                        List valueList = (List) entry.getValue();
                        for (Object value : valueList) {
                            if (value instanceof File) {
                                File file = (File) value;
                                multipartBuilder.addFormDataPart(entry.getKey(), file.getName(), RequestBody.create(MediaType.parse(getMimeType(file)), file));
                                if (DEBUGMODE && isShowLog()) {
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
                if (DEBUGMODE && isShowLog()) {
                    LockLog.Builder.create()
                            .e(">>>", "-------------------------------------")
                            .e(">>>", "创建请求失败:无上传的文件")
                            .e(">>>", "=====================================")
                            .build();
                }
                return null;
            }
            multipartBuilder = interceptMultipartBuilder(multipartBuilder);
            requestBody = createRequestBody(multipartBuilder.build());
        } else if (isJsonRequest) {
            requestInfo = new RequestInfo(url, jsonParameter, context.get().hashCode());
            RequestInfo sameRequestInfo = equalsRequestInfo(requestInfo);
            if (disallowSameRequest && sameRequestInfo != null) {
                return null;
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
                if (DEBUGMODE && isShowLog()) {
                    LockLog.Builder.create()
                            .e(">>>", "-------------------------------------")
                            .e(">>>", "创建请求失败:" + jsonParameter + " 不是正确的json格式参数")
                            .e(">>>", "=====================================")
                            .build();
                }
                return null;
            }
            requestBody = createRequestBody(RequestBody.create(MediaType.parse(getMimeType(requestInfo, httpCall, "application/json; charset=utf-8")), jsonParameter));
        } else if (isStringRequest) {
            requestInfo = new RequestInfo(url, stringParameter, context.get().hashCode());
            RequestInfo sameRequestInfo = equalsRequestInfo(requestInfo);
            if (disallowSameRequest && sameRequestInfo != null) {
                return null;
            }
            addRequestInfo(requestInfo);
            if (parameterInterceptListener != null) {
                try {
                    stringParameter = (String) parameterInterceptListener.onIntercept(context.get(), url, stringParameter);
                } catch (Exception e) {
                }
            }
            if (isNull(stringParameter)) {
                if (DEBUGMODE && isShowLog()) {
                    LockLog.Builder.create()
                            .e(">>>", "-------------------------------------")
                            .e(">>>", "创建请求失败:" + stringParameter)
                            .e(">>>", "=====================================")
                            .build();
                }
                return null;
            }
            requestBody = createRequestBody(RequestBody.create(MediaType.parse(getMimeType(requestInfo, httpCall, "text/plain; charset=utf-8")), stringParameter));
        } else {
            if (parameter != null) {
                requestInfo = new RequestInfo(url, parameter, context.get().hashCode());
                RequestInfo sameRequestInfo = equalsRequestInfo(requestInfo);
                if (disallowSameRequest && sameRequestInfo != null) {
                    return null;
                }
                addRequestInfo(requestInfo);
                if (parameterInterceptListener != null) {
                    try {
                        parameter = (Parameter) parameterInterceptListener.onIntercept(context.get(), url, parameter);
                    } catch (Exception e) {
                    }
                }
                requestBody = createRequestBody(parameter.toOkHttpParameter());
            }
        }

        //请求类型处理
        switch (requestType) {
            case GET_REQUEST:               //GET
                if (parameter != null) {
                    builder.url(url.contains("?") ? url + "&" + parameter.toParameterString() : url + "?" + parameter.toParameterString());
                } else {
                    builder.url(url);
                }
                break;
            case PUT_REQUEST:               //PUT
                builder.url(url);
                if (requestBody != null) builder.put(requestBody);
                break;
            case DELETE_REQUEST:            //DELETE
                builder.url(url);
                if (requestBody != null) builder.delete(requestBody);
                break;
            case PATCH_REQUEST:             //PATCH
                builder.url(url);
                if (requestBody != null) builder.patch(requestBody);
                break;
            default:                        //POST
                builder.url(url);
                if (requestBody != null) builder.post(requestBody);
                break;
        }

        //请求头处理
        if (DEBUGMODE && isShowLog()) {
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
            if (DEBUGMODE && isShowLog()) {
                LockLog.logI(">>>>>>", entry.getKey() + "=" + entry.getValue());
            }
        }
        if (!isNull(cookieStr)) {
            builder.addHeader("Cookie", cookieStr);
        }
        return builder.build();
    }

    private RequestBody createRequestBody(RequestBody requestBody) {
        if (uploadProgressListener != null) {
            return new RequestBodyImpl(requestBody) {
                @Override
                public void loading(long current, long total, boolean done) {
                    uploadProgressCallback(current, total, done);
                }
            };
        } else {
            return requestBody;
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

    private String getMimeType(RequestInfo requestInfo, Call httpCall, String defaultMimeType) {
        if (customMimeInterceptor == null) {
            return defaultMimeType;
        }
        String mimeType = customMimeInterceptor.onRequestMimeInterceptor(requestInfo, httpCall);
        if (isNull(mimeType)) return defaultMimeType;
        return mimeType;
    }

    public String getMimeType(File file) {
        if (customMimeInterceptor != null) {
            if (!isNull(customMimeInterceptor.onUploadFileMimeInterceptor(file))) {
                return customMimeInterceptor.onUploadFileMimeInterceptor(file);
            }
        }
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
            if (isNull(requestUrl) && DEBUGMODE && isShowLog()) {
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

            okHttpClient = createClient();
            if (okHttpClient == null) {
                return;
            }

            Request request = createRequest();
            if (request == null) {
                return;
            }

            if (DEBUGMODE && isShowLog()) {
                LockLog.Builder.create()
                        .i(">>>", "-------------------------------------")
                        .i(">>>", "开始下载:" + url)
                        .i(">>>", "=====================================")
                        .build();
            }
            httpCall = okHttpClient.newCall(request);
            if (isAsync()) {
                try {
                    Response response = httpCall.execute();
                    onDownloadFinish(response);
                } catch (Exception e) {
                    onDownloadFail(e);
                }
            } else {
                httpCall.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        onDownloadFail(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        onDownloadFinish(response);
                    }
                });
            }
        } catch (Exception e) {
            onDownloadFail(e);
        }
    }

    private void onDownloadFail(Exception e) {
        if (DEBUGMODE && isShowLog()) {
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

    private void onDownloadFinish(Response response) {
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
            runOnMain(new Runnable() {
                @Override
                public void run() {
                    onDownloadListener.onDownloadBegin(response, total);
                }
            });
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
            if (DEBUGMODE && isShowLog()) {
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
            if (DEBUGMODE && isShowLog()) {
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
                        if (DEBUGMODE && isShowLog()) {
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
                        if (DEBUGMODE && isShowLog()) {
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

    public void doPatch() {
        requestType = PATCH_REQUEST;
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

    /**
     * 请改为使用 setCustomMimeInterceptor(...)
     *
     * @param customMimeType
     * @return
     */
    @Deprecated
    public HttpRequest setCustomMimeType(String customMimeType) {
        this.customMimeType = customMimeType;
        return this;
    }

    @Deprecated
    public String getCustomMimeType() {
        return customMimeType;
    }

    public CustomMimeInterceptor getCustomMimeInterceptor() {
        return customMimeInterceptor;
    }

    public HttpRequest setCustomMimeInterceptor(CustomMimeInterceptor customMimeInterceptor) {
        this.customMimeInterceptor = customMimeInterceptor;
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

    public void onDetach() {
        if (context != null) {
            context.clear();
            context = null;
        }
        if (httpCall != null) {
            httpCall.cancel(); // 取消未完成的请求
            httpCall = null;
        }
    }

    // 新增方法：判断 context 是否为 LifecycleOwner
    private boolean isLifecycleOwner(Context context) {
        return context instanceof LifecycleOwner;
    }

    // 新增方法：观察 LifecycleOwner 的生命周期
    private void observeLifecycle(LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    // 生命周期结束，取消请求并清理 context
                    onDetach();
                }
            }
        });
    }

    // 修改 runOnMain 方法，增加对 LifecycleOwner 的支持
    private void runOnMain(Runnable runnable) {
        if (context == null || context.get() == null) {
            stop();
            return;
        }
        if (context.get() instanceof Activity) {
            Activity activity = (Activity) context.get();
            if (activity.isFinishing() || activity.isDestroyed()) { // 检查 Activity 是否已销毁
                stop();
                return;
            }
            activity.runOnUiThread(runnable);
        } else if (isLifecycleOwner(context.get())) {
            // 如果 context 是 LifecycleOwner，则注册生命周期观察者
            observeLifecycle((LifecycleOwner) context.get());
            runnable.run();
        } else {
            if (DEBUGMODE && DETAILSLOGS && isShowLog()) {
                LockLog.logI(">>>", "context 不是 Activity，本次请求在异步线程返回 >>>");
            }
            runnable.run();
        }
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

    public boolean isAsync() {
        if (async == null) return BaseOkHttp.async;
        return async || BaseOkHttp.async;
    }

    public HttpRequest setAsync(boolean async) {
        this.async = async;
        return this;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public HttpRequest setShowLog(boolean showLog) {
        this.showLog = showLog;
        return this;
    }
}
