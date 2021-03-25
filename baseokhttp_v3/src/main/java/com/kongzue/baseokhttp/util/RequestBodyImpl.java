package com.kongzue.baseokhttp.util;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public abstract class RequestBodyImpl extends RequestBody {
    
    private final RequestBody requestBody;
    private BufferedSink bufferedSink;
    
    public RequestBodyImpl(RequestBody requestBody) {
        this.requestBody = requestBody;
    }
    
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }
    
    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }
    
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }
        requestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
    }
    
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            private long current;
            private long total;
            private long last = 0;
            
            @Override
            public void write(okio.Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (total == 0) {
                    total = contentLength();
                }
                current += byteCount;
                long now = current;
                if (last < now) {
                    loading(now, total, total == current);
                    last = now;
                }
            }
        };
    }
    
    public abstract void loading(long current, long total, boolean done);
}
