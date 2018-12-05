package com.kongzue.baseokhttp.util;

import com.kongzue.baseokhttp.listener.ParameterInterceptListener;
import com.kongzue.baseokhttp.listener.ResponseInterceptListener;

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
    public static final int POST_JSON = 2;          //Json参数的POST
    public static final int POST_FILE = 3;          //文件类型的POST
    
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
    public static ResponseInterceptListener responseInterceptListener;
    
    //全局参数拦截器
    public static ParameterInterceptListener parameterInterceptListener;
    
    //全局请求头
    public static Parameter overallHeader;
    
    //全局参数
    public static Parameter overallParameter;
    
}
