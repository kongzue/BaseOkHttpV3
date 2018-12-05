//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public final class InflaterSource implements Source {
  private final BufferedSource source;
  private final Inflater inflater;
  private int bufferBytesHeldByInflater;
  private boolean closed;
  
  public InflaterSource(Source source, Inflater inflater) {
    this(Okio.buffer(source), inflater);
  }
  
  InflaterSource(BufferedSource source, Inflater inflater) {
    if (source == null) {
      throw new IllegalArgumentException("source == null");
    } else if (inflater == null) {
      throw new IllegalArgumentException("inflater == null");
    } else {
      this.source = source;
      this.inflater = inflater;
    }
  }
  
  public long read(Buffer sink, long byteCount) throws IOException {
    if (byteCount < 0L) {
      throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    } else if (this.closed) {
      throw new IllegalStateException("closed");
    } else if (byteCount == 0L) {
      return 0L;
    } else {
      while(true) {
        boolean sourceExhausted = this.refill();
        
        try {
          Segment tail = sink.writableSegment(1);
          int toRead = (int)Math.min(byteCount, (long)(8192 - tail.limit));
          int bytesInflated = this.inflater.inflate(tail.data, tail.limit, toRead);
          if (bytesInflated > 0) {
            tail.limit += bytesInflated;
            sink.size += (long)bytesInflated;
            return (long)bytesInflated;
          }
          
          if (this.inflater.finished() || this.inflater.needsDictionary()) {
            this.releaseInflatedBytes();
            if (tail.pos == tail.limit) {
              sink.head = tail.pop();
              SegmentPool.recycle(tail);
            }
            
            return -1L;
          }
          
          if (sourceExhausted) {
            throw new EOFException("source exhausted prematurely");
          }
        } catch (DataFormatException var8) {
          throw new IOException(var8);
        }
      }
    }
  }
  
  public boolean refill() throws IOException {
    if (!this.inflater.needsInput()) {
      return false;
    } else {
      this.releaseInflatedBytes();
      if (this.inflater.getRemaining() != 0) {
        throw new IllegalStateException("?");
      } else if (this.source.exhausted()) {
        return true;
      } else {
        Segment head = this.source.buffer().head;
        this.bufferBytesHeldByInflater = head.limit - head.pos;
        this.inflater.setInput(head.data, head.pos, this.bufferBytesHeldByInflater);
        return false;
      }
    }
  }
  
  private void releaseInflatedBytes() throws IOException {
    if (this.bufferBytesHeldByInflater != 0) {
      int toRelease = this.bufferBytesHeldByInflater - this.inflater.getRemaining();
      this.bufferBytesHeldByInflater -= toRelease;
      this.source.skip((long)toRelease);
    }
  }
  
  public Timeout timeout() {
    return this.source.timeout();
  }
  
  public void close() throws IOException {
    if (!this.closed) {
      this.inflater.end();
      this.closed = true;
      this.source.close();
    }
  }
}
