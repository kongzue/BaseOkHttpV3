//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.IOException;

public final class Pipe {
    final long maxBufferSize;
    final Buffer buffer = new Buffer();
    boolean sinkClosed;
    boolean sourceClosed;
    private final Sink sink = new Pipe.PipeSink();
    private final Source source = new Pipe.PipeSource();
    
    public Pipe(long maxBufferSize) {
        if (maxBufferSize < 1L) {
            throw new IllegalArgumentException("maxBufferSize < 1: " + maxBufferSize);
        } else {
            this.maxBufferSize = maxBufferSize;
        }
    }
    
    public Source source() {
        return this.source;
    }
    
    public Sink sink() {
        return this.sink;
    }
    
    final class PipeSource implements Source {
        final Timeout timeout = new Timeout();
        
        PipeSource() {
        }
        
        public long read(Buffer sink, long byteCount) throws IOException {
            Buffer var4 = Pipe.this.buffer;
            synchronized(Pipe.this.buffer) {
                if (Pipe.this.sourceClosed) {
                    throw new IllegalStateException("closed");
                } else {
                    while(Pipe.this.buffer.size() == 0L) {
                        if (Pipe.this.sinkClosed) {
                            return -1L;
                        }
                        
                        this.timeout.waitUntilNotified(Pipe.this.buffer);
                    }
                    
                    long result = Pipe.this.buffer.read(sink, byteCount);
                    Pipe.this.buffer.notifyAll();
                    return result;
                }
            }
        }
        
        public void close() throws IOException {
            Buffer var1 = Pipe.this.buffer;
            synchronized(Pipe.this.buffer) {
                Pipe.this.sourceClosed = true;
                Pipe.this.buffer.notifyAll();
            }
        }
        
        public Timeout timeout() {
            return this.timeout;
        }
    }
    
    final class PipeSink implements Sink {
        final Timeout timeout = new Timeout();
        
        PipeSink() {
        }
        
        public void write(Buffer source, long byteCount) throws IOException {
            Buffer var4 = Pipe.this.buffer;
            synchronized(Pipe.this.buffer) {
                if (Pipe.this.sinkClosed) {
                    throw new IllegalStateException("closed");
                } else {
                    while(byteCount > 0L) {
                        if (Pipe.this.sourceClosed) {
                            throw new IOException("source is closed");
                        }
                        
                        long bufferSpaceAvailable = Pipe.this.maxBufferSize - Pipe.this.buffer.size();
                        if (bufferSpaceAvailable == 0L) {
                            this.timeout.waitUntilNotified(Pipe.this.buffer);
                        } else {
                            long bytesToWrite = Math.min(bufferSpaceAvailable, byteCount);
                            Pipe.this.buffer.write(source, bytesToWrite);
                            byteCount -= bytesToWrite;
                            Pipe.this.buffer.notifyAll();
                        }
                    }
                    
                }
            }
        }
        
        public void flush() throws IOException {
            Buffer var1 = Pipe.this.buffer;
            synchronized(Pipe.this.buffer) {
                if (Pipe.this.sinkClosed) {
                    throw new IllegalStateException("closed");
                } else if (Pipe.this.sourceClosed && Pipe.this.buffer.size() > 0L) {
                    throw new IOException("source is closed");
                }
            }
        }
        
        public void close() throws IOException {
            Buffer var1 = Pipe.this.buffer;
            synchronized(Pipe.this.buffer) {
                if (!Pipe.this.sinkClosed) {
                    if (Pipe.this.sourceClosed && Pipe.this.buffer.size() > 0L) {
                        throw new IOException("source is closed");
                    } else {
                        Pipe.this.sinkClosed = true;
                        Pipe.this.buffer.notifyAll();
                    }
                }
            }
        }
        
        public Timeout timeout() {
            return this.timeout;
        }
    }
}
