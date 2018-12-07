package com.kongzue.baseokhttp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.kongzue.baseokhttp.exceptions.NetworkErrorException;
import com.kongzue.baseokhttp.exceptions.TimeOutException;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.baseokhttp.util.BaseOkHttp;
import com.kongzue.baseokhttp.util.JsonFormat;
import com.kongzue.baseokhttp.util.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
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
import baseokhttp3.MediaType;
import baseokhttp3.MultipartBody;
import baseokhttp3.OkHttpClient;
import baseokhttp3.Request;
import baseokhttp3.RequestBody;

import static com.kongzue.baseokhttp.util.BaseOkHttp.*;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/12/5 17:25
 */
public class HttpRequest {
    
    private OkHttpClient okHttpClient;
    private MediaType MEDIA_TYPE = MediaType.parse("image/png");
    
    private Parameter parameter;
    private Parameter headers;
    private Context context;
    private HttpRequest httpRequest;
    private ResponseListener listener;
    private String url;
    private String jsonParameter;
    private int requestType;
    
    private boolean isSending;
    
    //POST一步创建方法
    public static void POST(Context context, String url, Parameter parameter, ResponseListener listener) {
        POST(context, url, null, parameter, listener);
    }
    
    //POST一步创建总方法
    public static void POST(Context context, String url, Parameter headers, Parameter parameter, ResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = context;
            httpRequest.headers = headers;
            httpRequest.listener = listener;
            httpRequest.parameter = parameter;
            httpRequest.url = url;
            httpRequest.requestType = POST_REQUEST;
            httpRequest.httpRequest = httpRequest;
            httpRequest.send();
        }
    }
    
    //JSON格式POST一步创建方法
    public static void JSONPOST(Context context, String url, String jsonParameter, ResponseListener listener) {
        JSONPOST(context, url, null, jsonParameter, listener);
    }
    
    //JSON格式POST一步创建总方法
    public static void JSONPOST(Context context, String url, Parameter headers, String jsonParameter, ResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = context;
            httpRequest.headers = headers;
            httpRequest.listener = listener;
            httpRequest.jsonParameter = jsonParameter;
            httpRequest.url = url;
            httpRequest.requestType = POST_JSON;
            httpRequest.httpRequest = httpRequest;
            httpRequest.send();
        }
    }
    
    //GET一步创建方法
    public static void GET(Context context, String url, Parameter parameter, ResponseListener listener) {
        GET(context, url, null, parameter, listener);
    }
    
    //GET一步创建总方法
    public static void GET(Context context, String url, Parameter headers, Parameter parameter, ResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = context;
            httpRequest.headers = headers;
            httpRequest.listener = listener;
            httpRequest.parameter = parameter;
            httpRequest.url = url;
            httpRequest.requestType = GET_REQUEST;
            httpRequest.httpRequest = httpRequest;
            httpRequest.send();
        }
    }
    
    private void send() {
        if (parameter != null && !parameter.entrySet().isEmpty()) {
            for (Map.Entry<String, Object> entry : parameter.entrySet()) {
                if (entry.getValue() instanceof File) {
                    requestType = POST_FILE;
                    break;
                }
            }
        }
        if (!isNull(jsonParameter)) {
            requestType = POST_JSON;
        }
        try {
            //全局参数拦截处理
            if (parameterInterceptListener != null) {
                parameter = parameterInterceptListener.onIntercept(parameter);
            }
            
            //全局参数
            if (overallParameter != null && !overallParameter.entrySet().isEmpty()) {
                for (Map.Entry<String, Object> entry : overallParameter.entrySet()) {
                    parameter.add(entry.getKey(), entry.getValue());
                }
            }
            
            if (!url.startsWith("http")) {
                url = serviceUrl + url;
            }
            if (isNull(url)) {
                Log.e(">>>", "-------------------------------------");
                Log.e(">>>", "创建请求失败: 请求地址不能为空");
                Log.e(">>>", "=====================================");
            }
            
            if (SSLInAssetsFileName == null || SSLInAssetsFileName.isEmpty()) {
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(BaseOkHttp.TIME_OUT_DURATION, TimeUnit.SECONDS)
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        })
                        .build();
            } else {
                okHttpClient = getOkHttpClient(context, context.getAssets().open(SSLInAssetsFileName));
            }
            
            //创建请求
            baseokhttp3.Request request;
            baseokhttp3.Request.Builder builder = new Request.Builder();
            
            switch (requestType) {
                case POST_JSON:
                    if (isNull(jsonParameter)) {
                        if (DEBUGMODE) {
                            Log.e(">>>", "-------------------------------------");
                            Log.e(">>>", "创建请求失败:" + jsonParameter + " 不是正确的json格式参数");
                            Log.e(">>>", "=====================================");
                        }
                        return;
                    }
                    RequestBody jsonRequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParameter);
                    builder.url(url);
                    builder.post(jsonRequestBody);
                    break;
                case POST_FILE:
                    MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    
                    if (parameter != null && !parameter.entrySet().isEmpty()) {
                        for (Map.Entry<String, Object> entry : parameter.entrySet()) {
                            if (entry.getValue() instanceof File) {
                                File file = (File) entry.getValue();
                                multipartBuilder.addFormDataPart(entry.getKey(), file.getName(), RequestBody.create(MEDIA_TYPE, file));
                                if (DEBUGMODE)
                                    Log.i(">>>", "添加图片：" + entry.getKey() + ":" + file.getName());
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
                    MultipartBody multipartRequestBody = multipartBuilder.build();
                    builder.url(url);
                    builder.post(multipartRequestBody);
                    break;
                default:
                    if (parameter == null) parameter = new Parameter();
                    builder = new baseokhttp3.Request.Builder();
                    RequestBody requestBody = parameter.toOkHttpParameter();
                    
                    //请求类型处理
                    if (requestType == GET_REQUEST) {
                        builder.url(url + "?" + parameter.toParameterString());
                    } else {
                        builder.url(url);
                        builder.post(requestBody);
                    }
                    break;
            }
            
            //请求头处理
            if (DEBUGMODE) Log.i(">>>", "添加请求头:");
            if (overallHeader != null && !overallHeader.entrySet().isEmpty()) {
                for (Map.Entry<String, Object> entry : overallHeader.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue() + "");
                    if (DEBUGMODE) Log.i(">>>>>>", entry.getKey() + "=" + entry.getValue());
                }
            }
            if (headers != null && !headers.entrySet().isEmpty()) {
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue() + "");
                    if (DEBUGMODE) Log.i(">>>>>>", entry.getKey() + "=" + entry.getValue());
                }
            }
            
            request = builder.build();
            
            if (DEBUGMODE) {
                Log.i(">>>", "-------------------------------------");
                Log.i(">>>", "创建请求:" + url);
                Log.i(">>>", "参数:");
                if (requestType == POST_JSON) {
                    if (!JsonFormat.formatJson(jsonParameter)){
                        Log.i(">>>>>>", jsonParameter);
                    }
                }else{
                    parameter.toPrintString();
                }
                Log.i(">>>", "请求已发送 ->");
            }
            
            isSending = true;
            checkTimeOut();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    if (!isSending) return;
                    isSending = false;
                    if (DEBUGMODE) {
                        Log.e(">>>", "请求失败:" + url);
                        Log.e(">>>", "参数:");
                        if (requestType == POST_JSON) {
                            if (!JsonFormat.formatJson(jsonParameter,1)){
                                Log.e(">>>>>>", jsonParameter);
                            }
                        }else{
                            parameter.toPrintString(1);
                        }
                        Log.e(">>>", "错误:" + e.toString());
                        Log.e(">>>", "=====================================");
                    }
                    //回到主线程处理
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (responseInterceptListener != null) {
                                    if (responseInterceptListener.onResponse(context, url, null, new NetworkErrorException())) {
                                        if (listener != null)
                                            listener.onResponse(null, new NetworkErrorException());
                                    }
                                } else {
                                    if (listener != null)
                                        listener.onResponse(null, new NetworkErrorException());
                                }
                            }
                        });
                    } else {
                        if (responseInterceptListener != null) {
                            if (responseInterceptListener.onResponse(context, url, null, new NetworkErrorException())) {
                                if (listener != null)
                                    listener.onResponse(null, new NetworkErrorException());
                            }
                        } else {
                            if (listener != null)
                                listener.onResponse(null, new NetworkErrorException());
                        }
                    }
                    
                }
                
                @Override
                public void onResponse(Call call, baseokhttp3.Response response) throws IOException {
                    if (!isSending) return;
                    isSending = false;
                    final String strResponse = response.body().string();
                    if (DEBUGMODE) {
                        Log.i(">>>", "请求成功:" + url);
                        Log.i(">>>", "参数:");
                        if (requestType == POST_JSON) {
                            if (!JsonFormat.formatJson(jsonParameter)){
                                Log.i(">>>>>>", jsonParameter);
                            }
                        }else{
                            parameter.toPrintString();
                        }
                        Log.i(">>>", "返回内容:");
                        if (!JsonFormat.formatJson(strResponse)) {
                            Log.i(">>>", strResponse);
                        }
                        Log.i(">>>", "=====================================");
                    }
                    
                    //回到主线程处理
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (responseInterceptListener != null) {
                                    if (responseInterceptListener.onResponse(context, url, strResponse, null)) {
                                        if (listener != null)
                                            listener.onResponse(strResponse, null);
                                    }
                                } else {
                                    if (listener != null) listener.onResponse(strResponse, null);
                                }
                            }
                        });
                    } else {
                        if (responseInterceptListener != null) {
                            if (responseInterceptListener.onResponse(context, url, strResponse, null)) {
                                if (listener != null) listener.onResponse(strResponse, null);
                            }
                        } else {
                            if (listener != null) listener.onResponse(strResponse, null);
                        }
                    }
                }
            });
        } catch (Exception e) {
            if (DEBUGMODE) {
                Log.e(">>>", "请求创建失败:" + url);
                Log.e(">>>", "参数:");
                if (requestType == POST_JSON) {
                    if (!JsonFormat.formatJson(jsonParameter,1)){
                        Log.e(">>>>>>", jsonParameter);
                    }
                }else{
                    parameter.toPrintString(1);
                }
                Log.e(">>>", "错误:" + e.toString());
                e.printStackTrace();
                Log.e(">>>", "=====================================");
            }
        }
    }
    
    private Timer timer;
    
    private void checkTimeOut() {
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isSending && listener != null) {
                    isSending = false;
                    Log.e(">>>", "请求超时 ×");
                    Log.e(">>>", "=====================================");
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResponse(null, new TimeOutException());
                            }
                        });
                    } else {
                        listener.onResponse(null, new TimeOutException());
                    }
                }
            }
        }, TIME_OUT_DURATION * 1000);
    }
    
    private OkHttpClient getOkHttpClient(Context context, InputStream... certificates) {
        if (okHttpClient == null) {
            File sdcache = context.getExternalCacheDir();
            int cacheSize = 10 * 1024 * 1024;
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(BaseOkHttp.TIME_OUT_DURATION, TimeUnit.SECONDS)
                    .writeTimeout(BaseOkHttp.TIME_OUT_DURATION, TimeUnit.SECONDS)
                    .readTimeout(BaseOkHttp.TIME_OUT_DURATION, TimeUnit.SECONDS)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            if (DEBUGMODE)
                                Log.i("<<<", "hostnameVerifier: " + hostname);
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
                    if (certificate != null)
                        certificate.close();
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
        if (s == null || s.trim().isEmpty() || s.equals("null") || s.equals("(null)")) {
            return true;
        }
        return false;
    }
    
    private HttpRequest() {
    }
    
    public static HttpRequest build(Context context, String url) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = context;
            httpRequest.url = url;
            httpRequest.httpRequest = httpRequest;
            return httpRequest;
        }
    }
    
    public HttpRequest addParameter(String key, Object value) {
        if (parameter == null) parameter = new Parameter();
        parameter.add(key, value);
        this.jsonParameter = null;
        return this;
    }
    
    public HttpRequest setParameter(Parameter parameter) {
        this.parameter = parameter;
        this.jsonParameter = null;
        return this;
    }
    
    public HttpRequest setParameter(String jsonParameter) {
        this.jsonParameter = jsonParameter;
        this.parameter = null;
        return this;
    }
    
    public HttpRequest setJsonParameter(String jsonParameter) {
        this.jsonParameter = jsonParameter;
        return this;
    }
    
    public HttpRequest addHeaders(String key, String value) {
        if (headers == null) headers = new Parameter();
        headers.add(key, value);
        return this;
    }
    
    public HttpRequest setHeaders(Parameter headers) {
        this.headers = headers;
        return this;
    }
    
    public HttpRequest setUrl(String url) {
        this.url = url;
        return this;
    }
    
    public HttpRequest setResponseListener(ResponseListener listener) {
        this.listener = listener;
        return this;
    }
    
    public HttpRequest doPost() {
        send();
        requestType = POST_REQUEST;
        return this;
    }
    
    public HttpRequest doGet() {
        send();
        requestType = GET_REQUEST;
        return this;
    }
    
    public HttpRequest setMediaType(MediaType mediaType) {
        MEDIA_TYPE = mediaType;
        return this;
    }
}
