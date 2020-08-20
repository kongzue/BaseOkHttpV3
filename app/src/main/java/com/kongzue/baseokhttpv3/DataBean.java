package com.kongzue.baseokhttpv3;

import java.util.List;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/7/31 18:38
 */
public class DataBean {
    
    private int code;
    private String message;
    private List<Result> result;
    
    public DataBean() {
    }
    
    public DataBean(int code, String message, List<Result> result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }
    
    public int getCode() {
        return code;
    }
    
    public DataBean setCode(int code) {
        this.code = code;
        return this;
    }
    
    public String getMessage() {
        return message;
    }
    
    public DataBean setMessage(String message) {
        this.message = message;
        return this;
    }
    
    public List<Result> getResult() {
        return result;
    }
    
    public DataBean setResult(List<Result> result) {
        this.result = result;
        return this;
    }
    
    @Override
    public String toString() {
        String list = "[";
        if (result!=null) {
            for (Result r : result) {
                list = list + r + ",\n";
            }
        }
        list = list + "]";
        return "DataBean{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", result=" + list +
                '}';
    }
    
    public static class Result {
    
        public Result(String path, String image, String title, String time) {
            this.path = path;
            this.image = image;
            this.title = title;
            this.time = time;
        }
    
        public Result() {
        }
    
        private String path;
        private String image;
        private String title;
        private String time;
        private boolean visible;
        
        public String getPath() {
            return path;
        }
        
        public Result setPath(String path) {
            this.path = path;
            return this;
        }
        
        public String getImage() {
            return image;
        }
        
        public Result setImage(String image) {
            this.image = image;
            return this;
        }
        
        public String getTitle() {
            return title;
        }
        
        public Result setTitle(String title) {
            this.title = title;
            return this;
        }
        
        public String getTime() {
            return time;
        }
        
        public Result setTime(String time) {
            this.time = time;
            return this;
        }
    
        public boolean isVisible() {
            return visible;
        }
    
        public Result setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }
    
        @Override
        public String toString() {
            return "Result{" +
                    "path='" + path + '\'' +
                    ", image='" + image + '\'' +
                    ", title='" + title + '\'' +
                    ", time='" + time + '\'' +
                    '}';
        }
    }
}
