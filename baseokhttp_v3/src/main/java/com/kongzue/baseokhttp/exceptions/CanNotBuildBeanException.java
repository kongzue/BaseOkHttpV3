package com.kongzue.baseokhttp.exceptions;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/7/31 17:44
 */
public class CanNotBuildBeanException extends Exception {
    public CanNotBuildBeanException(String reason){
        super("无法创建 Bean 目标类：" + reason);
    }
}
