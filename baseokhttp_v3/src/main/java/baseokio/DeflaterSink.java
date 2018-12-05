//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.IOException;
import java.util.zip.Deflater;

public final class DeflaterSink implements Sink {
  private final BufferedSink sink;
  private final Deflater deflater;
  private boolean closed;
  
  public DeflaterSink(Sink sink, Deflater deflater) {
    this(Okio.buffer(sink), deflater);
  }
  
  DeflaterSink(BufferedSink sink, Deflater deflater) {
    if (sink == null) {
      throw new IllegalArgumentException("source == null");
    } else if (deflater == null) {
      throw new IllegalArgumentException("inflater == null");
    } else {
      this.sink = sink;
      this.deflater = deflater;
    }
  }
  
  public void write(Buffer source, long byteCount) throws IOException {
    Util.checkOffsetAndCount(source.size, 0L, byteCount);
    
    int toDeflate;
    for(; byteCount > 0L; byteCount -= (long)toDeflate) {
      Segment head = source.head;
      toDeflate = (int)Math.min(byteCount, (long)(head.limit - head.pos));
      this.deflater.setInput(head.data, head.pos, toDeflate);
      this.deflate(false);
      source.size -= (long)toDeflate;
      head.pos += toDeflate;
      if (head.pos == head.limit) {
        source.head = head.pop();
        SegmentPool.recycle(head);
      }
    }
    
  }
  
  private void deflate(boolean syncFlush) throws IOException {
    Buffer buffer = this.sink.buffer();
    
    while(true) {
      Segment s = buffer.writableSegment(1);
      int deflated = syncFlush ? this.deflater.deflate(s.data, s.limit, 8192 - s.limit, 2) : this.deflater.deflate(s.data, s.limit, 8192 - s.limit);
      if (deflated > 0) {
        s.limit += deflated;
        buffer.size += (long)deflated;
        this.sink.emitCompleteSegments();
      } else if (this.deflater.needsInput()) {
        if (s.pos == s.limit) {
          buffer.head = s.pop();
          SegmentPool.recycle(s);
        }
        
        return;
      }
    }
  }
  
  public void flush() throws IOException {
    this.deflate(true);
    this.sink.flush();
  }
  
  void finishDeflate() throws IOException {
    this.deflater.finish();
    this.deflate(false);
  }
  
  public void close() throws IOException {
    if (!this.closed) {
      Throwable thrown = null;
      
      try {
        this.finishDeflate();
      } catch (Throwable var3) {
        thrown = var3;
      }
      
      try {
        this.deflater.end();
      } catch (Throwable var5) {
        if (thrown == null) {
          thrown = var5;
        }
      }
      
      try {
        this.sink.close();
      } catch (Throwable var4) {
        if (thrown == null) {
          thrown = var4;
        }
      }
      
      this.closed = true;
      if (thrown != null) {
        Util.sneakyRethrow(thrown);
      }
      
    }
  }
  
  public Timeout timeout() {
    return this.sink.timeout();
  }
  
  public String toString() {
    return "DeflaterSink(" + this.sink + ")";
  }
}
