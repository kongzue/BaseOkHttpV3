package com.kongzue.baseokhttp.util;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/7/23 11:19
 */
public class LockLog {
    
    private static List<LogBody> logS;
    
    public static void logI(String tag, String s) {
        synchronized (LockLog.class) {
            if (logS == null) {
                logS = new ArrayList<>();
            }
            logS.add(new LogBody(LogBody.LEVEL.INFO, tag, s));
            logPrint();
        }
    }
    
    public static void logE(String tag, String s) {
        synchronized (LockLog.class) {
            if (logS == null) {
                logS = new ArrayList<>();
            }
            logS.add(new LogBody(LogBody.LEVEL.ERROR, tag, s));
            logPrint();
        }
    }
    
    public static void log(List<LogBody> s) {
        synchronized (LockLog.class) {
            if (logS == null) {
                logS = new ArrayList<>();
            }
            logS.addAll(s);
            logPrint();
        }
    }
    
    private static Thread logThread;
    
    private static void logPrint() {
        if (logThread == null) {
            logThread = new Thread() {
                @Override
                public void run() {
                    if (logS != null) {
                        while (!logS.isEmpty()) {
                            LogBody log = logS.get(0);
                            if (log != null) {
                                switch (log.getLevel()) {
                                    case INFO:
                                        Log.i(log.getTag(), log.getLog());
                                        break;
                                    case ERROR:
                                        Log.e(log.getTag(), log.getLog());
                                        break;
                                }
                                logS.remove(log);
                            }
                        }
                        logThread = null;
                    }
                }
            };
            logThread.start();
        }
    }
    
    public static class LogBody {
        
        public enum LEVEL {
            INFO,
            ERROR
        }
        
        String tag, log;
        LEVEL level;
        
        public LogBody(LEVEL level, String tag, String log) {
            this.tag = tag;
            this.log = log;
            this.level = level;
        }
        
        public LogBody(String tag, String log) {
            this.tag = tag;
            this.log = log;
            this.level = LEVEL.INFO;
        }
        
        public String getTag() {
            return tag == null ? ">>>" : tag;
        }
        
        public LogBody setTag(String tag) {
            this.tag = tag;
            return this;
        }
        
        public LEVEL getLevel() {
            return level;
        }
        
        public String getLog() {
            return log == null ? "" : log;
        }
        
        public LogBody setLog(String log) {
            this.log = log;
            return this;
        }
    }
    
    public static class Builder {
        
        private List<LogBody> list;
        
        private Builder() {
            list = new ArrayList<>();
        }
        
        public static Builder create() {
            return new Builder();
        }
        
        public Builder i(String tag, String s) {
            list.add(new LogBody(tag, s));
            return this;
        }
    
        public Builder e(String tag, String s) {
            list.add(new LogBody(LogBody.LEVEL.ERROR,tag, s));
            return this;
        }
        
        public Builder add(List<LogBody> l) {
            list.addAll(l);
            return this;
        }
        
        public void build(){
            LockLog.log(list);
        }
    }
    
    public static String getExceptionInfo(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}
