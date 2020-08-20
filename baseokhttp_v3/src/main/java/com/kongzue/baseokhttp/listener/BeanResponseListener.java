package com.kongzue.baseokhttp.listener;

import android.util.Log;

import com.kongzue.baseokhttp.exceptions.CanNotBuildBeanException;
import com.kongzue.baseokhttp.exceptions.DecodeJsonException;
import com.kongzue.baseokhttp.util.JsonBean;
import com.kongzue.baseokhttp.util.JsonList;
import com.kongzue.baseokhttp.util.JsonMap;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/7/31 17:37
 */
public abstract class BeanResponseListener<T> implements BaseResponseListener {
    
    @Override
    public void response(Object response, Exception error) {
        if (error == null) {
            T tInstance = null;
            Class<T> tClass;
            try {
                ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
                tClass = (Class<T>) pt.getActualTypeArguments()[0];
            } catch (Exception e) {
                onResponse(null, new CanNotBuildBeanException("请检查该 Bean 是否为 public 且其构造方法为 public"));
                return;
            }
            
            JsonMap data = new JsonMap(response.toString());
            if (data.isEmpty()) {
                onResponse(null, new DecodeJsonException(response.toString()));
            }
            tInstance = JsonBean.getBean(data, tClass);
            
            onResponse(tInstance, null);
        } else {
            onResponse(null, error);
        }
    }
    
    public abstract void onResponse(T main, Exception error);
}