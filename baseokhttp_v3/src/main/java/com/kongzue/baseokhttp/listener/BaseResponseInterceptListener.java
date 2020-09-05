package com.kongzue.baseokhttp.listener;

import android.content.Context;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/8/28 15:27
 */
public interface BaseResponseInterceptListener {
    /**
     * 此方法用于内部传递未处理的返回值，请使用 ResponseInterceptListener 的具体实现
     * 返回值：为 true 时代表准许放行，继续回到原回调方法执行，为 false 时拦截不继续处理
     *
     * @hide
     * @param response
     * @param error
     */
    boolean response(Context context, String url, String response, Exception error);
}
