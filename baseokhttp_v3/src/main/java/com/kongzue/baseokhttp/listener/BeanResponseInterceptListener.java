package com.kongzue.baseokhttp.listener;

import android.content.Context;

import com.kongzue.baseokhttp.exceptions.NewInstanceBeanException;
import com.kongzue.baseokhttp.exceptions.DecodeJsonException;
import com.kongzue.baseokhttp.util.JsonBean;
import com.kongzue.baseokhttp.util.JsonMap;

import java.lang.reflect.ParameterizedType;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/8/28 15:22
 */
public abstract class BeanResponseInterceptListener<T> implements BaseResponseInterceptListener {
    @Override
    public boolean response(Context context, String url, String response, Exception error) {
        T tInstance = null;
        Class<T> tClass;
        try {
            ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
            tClass = (Class<T>) pt.getActualTypeArguments()[0];
            tInstance = tClass.newInstance();
        } catch (Exception e) {
            //这种情况下没办法实例化泛型对象
            return onResponse(context, url, null, new NewInstanceBeanException("请检查该 Bean 是否为 public 且其构造方法为 public"));
        }
        if (error == null) {
            JsonMap data = new JsonMap(response.toString());
            if (data.isEmpty()) {
                return onResponse(context, url, tInstance, new DecodeJsonException(response.toString()));
            }
            tInstance = JsonBean.getBean(data, tClass);
            
            return onResponse(context, url, tInstance, null);
        } else {
            return onResponse(context, url, tInstance, error);
        }
    }
    
    public abstract boolean onResponse(Context context, String url, T response, Exception error);
}
