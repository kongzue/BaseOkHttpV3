package com.kongzue.baseokhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.kongzue.baseokhttp.listener.WebSocketStatusListener;

import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static com.kongzue.baseokhttp.util.BaseOkHttp.*;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/12/31 17:20
 * reDesign from @WinRoad(https://github.com/hhh5022456)
 */
public class BaseWebSocket {
    
    //连接状态
    public final static int CONNECTING = 0;                         //连接中
    public final static int CONNECTED = 1;                          //已连接
    public final static int RECONNECT = 2;                          //重连中
    public final static int DISCONNECTED = -1;                      //断开
    
    public final static int BREAK_NORMAL = 0;                       //正常断开
    public final static int BREAK_ABNORMAL = 1;                     //异常断开
    
    private WeakReference<Context> context;
    private String url;
    private WebSocket webSocket;
    private OkHttpClient okHttpClient;
    private Request request;
    private int status = DISCONNECTED;
    private boolean autoReconnect = true;                           //自动重连
    private boolean manualClose = false;                            //手动关闭
    private WebSocketStatusListener webSocketStatusListener;
    private int reconnectCount = 0;                                 //重连次数
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    
    //创建方法
    public static BaseWebSocket BUILD(Context context, String url) {
        synchronized (BaseWebSocket.class) {
            BaseWebSocket baseWebSocket = new BaseWebSocket();
            baseWebSocket.context = new WeakReference<Context>(context);
            baseWebSocket.url = url;
            return baseWebSocket;
        }
    }
    
    //创建方法
    public static BaseWebSocket BUILD(Context context, String url, OkHttpClient okHttpClient) {
        synchronized (BaseWebSocket.class) {
            BaseWebSocket baseWebSocket = new BaseWebSocket();
            baseWebSocket.context = new WeakReference<Context>(context);
            baseWebSocket.url = url;
            baseWebSocket.okHttpClient = okHttpClient;
            return baseWebSocket;
        }
    }
    
    private void doConnect() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();
        }
        if (request == null) {
            request = new Request.Builder()
                    .url(url)
                    .build();
        }
        okHttpClient.dispatcher().cancelAll();
        okHttpClient.newWebSocket(request, webSocketListener);
    }
    
    //开始连接
    public BaseWebSocket startConnect() {
        manualClose = false;
        buildConnect();
        return this;
    }
    
    private void buildConnect() {
        if (!isNetworkConnected(context.get())) {
            status = DISCONNECTED;
        }
        if (status != CONNECTED && status != CONNECTING) {
            status = CONNECTING;
            doConnect();
        }
    }
    
    //尝试重新连接
    public void reConnect() {
        manualClose = true;
        if (!autoReconnect | manualClose) {
            return;
        }
        if (DEBUGMODE) {
            loge("重连次数：" + reconnectCount);
        }
        if (!isNetworkConnected(context.get())) {
            status = DISCONNECTED;
            loge("网络错误");
        }
        
        status = RECONNECT;
        long delay = reconnectCount * websocketReconnectInterval * 1000;
        mainHandler.postDelayed(reconnectRunnable, delay);
        reconnectCount++;
    }
    
    private Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (webSocketStatusListener != null) {
                webSocketStatusListener.onReconnect();
            }
            buildConnect();
        }
    };
    
    //断开连接
    public void disConnect() {
        if (status == DISCONNECTED) {
            return;
        }
        if (okHttpClient != null) {
            okHttpClient.dispatcher().cancelAll();
        }
        if (webSocket != null) {
            try{
                boolean isClosed = webSocket.close(1000, "normal close");
                //非正常关闭连接
                if (!isClosed) {
                    if (webSocketStatusListener != null) {
                        webSocketStatusListener.onDisconnected(BREAK_ABNORMAL);
                    }
                }
            }catch (Exception e){
                if (webSocketStatusListener != null) {
                    webSocketStatusListener.onDisconnected(BREAK_ABNORMAL);
                }
            }
        }
        status = DISCONNECTED;
    }
    
    //发送消息
    public boolean send(String msg) {
        return sendMsg(msg);
    }
    
    public boolean send(ByteString byteString) {
        return sendMsg(byteString);
    }
    
    private boolean sendMsg(Object msg) {
        boolean isSend = false;
        if (webSocket != null && status == CONNECTED) {
            if (msg instanceof String) {
                isSend = webSocket.send((String) msg);
            } else if (msg instanceof ByteString) {
                isSend = webSocket.send((ByteString) msg);
            }
            //发送消息失败，尝试重连
            if (!isSend) {
                loge("发送失败，尝试重连...");
                reConnect();
            }
        } else {
            loge("未建立连接，无法发送消息");
        }
        return isSend;
    }
    
    private WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket ws,final Response response) {
            logd("已建立连接");
            webSocket = ws;
            status = CONNECTED;
            reconnectCount = 0;
            mainHandler.removeCallbacks(reconnectRunnable);
            if (webSocketStatusListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSocketStatusListener.connected(response);
                        }
                    });
                } else {
                    webSocketStatusListener.connected(response);
                }
            }
        }
        
        @Override
        public void onMessage(WebSocket webSocket, final String text) {
            logd("接收到消息:" + text);
            if (webSocketStatusListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSocketStatusListener.onMessage(text);
                        }
                    });
                } else {
                    webSocketStatusListener.onMessage(text);
                }
            }
        }
        
        @Override
        public void onMessage(WebSocket webSocket,final ByteString bytes) {
            logd("接收到消息:" + bytes);
            if (webSocketStatusListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSocketStatusListener.onMessage(bytes);
                        }
                    });
                } else {
                    webSocketStatusListener.onMessage(bytes);
                }
            }
        }
        
        @Override
        public void onClosing(WebSocket webSocket, final int code, String reason) {
            logd("连接正在断开:" + code);
        }
        
        @Override
        public void onClosed(WebSocket webSocket, final int code, String reason) {
            logd("连接已断开:" + code);
            if (webSocketStatusListener != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSocketStatusListener.onDisconnected(BREAK_NORMAL);
                        }
                    });
                } else {
                    webSocketStatusListener.onDisconnected(BREAK_NORMAL);
                }
            }
        }
        
        @Override
        public void onFailure(WebSocket webSocket, final Throwable t, Response response) {
            if (status == DISCONNECTED){
                logd("连接已断开");
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webSocketStatusListener.onDisconnected(BREAK_ABNORMAL);
                        }
                    });
                } else {
                    webSocketStatusListener.onDisconnected(BREAK_ABNORMAL);
                }
                return;
            }
            try {
                loge("连接失败");
                reConnect();
                if (webSocketStatusListener != null) {
                    if (Looper.myLooper() != Looper.getMainLooper()) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                webSocketStatusListener.onConnectionFailed(t);
                            }
                        });
                    } else {
                        webSocketStatusListener.onConnectionFailed(t);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    
    //设置是否自动重连
    public BaseWebSocket setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }
    
    //设置返回监听器
    public BaseWebSocket setWebSocketStatusListener(WebSocketStatusListener webSocketStatusListener) {
        this.webSocketStatusListener = webSocketStatusListener;
        return this;
    }
    
    //返回重连次数
    public int getReconnectCount() {
        return reconnectCount;
    }
    
    //校验网络状态
    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission")
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
    
    //获取当前连接状态
    public int getStatus() {
        return status;
    }
    
    private void logd(String s) {
        if (DEBUGMODE)Log.d(">>>", s);
    }
    
    private void loge(String s) {
        if (DEBUGMODE)Log.e(">>>", s);
    }
    
    public void onDetach(){
        context.clear();
    }
}
