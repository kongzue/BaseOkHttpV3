//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

final class RealBufferedSink implements BufferedSink {
  public final Buffer buffer = new Buffer();
  public final Sink sink;
  boolean closed;
  
  RealBufferedSink(Sink sink) {
    if (sink == null) {
      throw new NullPointerException("sink == null");
    } else {
      this.sink = sink;
    }
  }
  
  public Buffer buffer() {
    return this.buffer;
  }
  
  public void write(Buffer source, long byteCount) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.write(source, byteCount);
      this.emitCompleteSegments();
    }
  }
  
  public BufferedSink write(ByteString byteString) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.write(byteString);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeUtf8(String string) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeUtf8(string);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeUtf8(string, beginIndex, endIndex);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeUtf8CodePoint(int codePoint) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeUtf8CodePoint(codePoint);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeString(String string, Charset charset) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeString(string, charset);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeString(String string, int beginIndex, int endIndex, Charset charset) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeString(string, beginIndex, endIndex, charset);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink write(byte[] source) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.write(source);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink write(byte[] source, int offset, int byteCount) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.write(source, offset, byteCount);
      return this.emitCompleteSegments();
    }
  }
  
  public int write(ByteBuffer source) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      int result = this.buffer.write(source);
      this.emitCompleteSegments();
      return result;
    }
  }
  
  public long writeAll(Source source) throws IOException {
    if (source == null) {
      throw new IllegalArgumentException("source == null");
    } else {
      long totalBytesRead = 0L;
      
      long readCount;
      while((readCount = source.read(this.buffer, 8192L)) != -1L) {
        totalBytesRead += readCount;
        this.emitCompleteSegments();
      }
      
      return totalBytesRead;
    }
  }
  
  public BufferedSink write(Source source, long byteCount) throws IOException {
    while(byteCount > 0L) {
      long read = source.read(this.buffer, byteCount);
      if (read == -1L) {
        throw new EOFException();
      }
      
      byteCount -= read;
      this.emitCompleteSegments();
    }
    
    return this;
  }
  
  public BufferedSink writeByte(int b) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeByte(b);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeShort(int s) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeShort(s);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeShortLe(int s) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeShortLe(s);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeInt(int i) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeInt(i);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeIntLe(int i) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeIntLe(i);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeLong(long v) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeLong(v);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeLongLe(long v) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeLongLe(v);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeDecimalLong(long v) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeDecimalLong(v);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink writeHexadecimalUnsignedLong(long v) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      this.buffer.writeHexadecimalUnsignedLong(v);
      return this.emitCompleteSegments();
    }
  }
  
  public BufferedSink emitCompleteSegments() throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      long byteCount = this.buffer.completeSegmentByteCount();
      if (byteCount > 0L) {
        this.sink.write(this.buffer, byteCount);
      }
      
      return this;
    }
  }
  
  public BufferedSink emit() throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      long byteCount = this.buffer.size();
      if (byteCount > 0L) {
        this.sink.write(this.buffer, byteCount);
      }
      
      return this;
    }
  }
  
  public OutputStream outputStream() {
    return new OutputStream() {
      public void write(int b) throws IOException {
        if (RealBufferedSink.this.closed) {
          throw new IOException("closed");
        } else {
          RealBufferedSink.this.buffer.writeByte((byte)b);
          RealBufferedSink.this.emitCompleteSegments();
        }
      }
      
      public void write(byte[] data, int offset, int byteCount) throws IOException {
        if (RealBufferedSink.this.closed) {
          throw new IOException("closed");
        } else {
          RealBufferedSink.this.buffer.write(data, offset, byteCount);
          RealBufferedSink.this.emitCompleteSegments();
        }
      }
      
      public void flush() throws IOException {
        if (!RealBufferedSink.this.closed) {
          RealBufferedSink.this.flush();
        }
        
      }
      
      public void close() throws IOException {
        RealBufferedSink.this.close();
      }
      
      public String toString() {
        return RealBufferedSink.this + ".outputStream()";
      }
    };
  }
  
  public void flush() throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      if (this.buffer.size > 0L) {
        this.sink.write(this.buffer, this.buffer.size);
      }
      
      this.sink.flush();
    }
  }
  
  public boolean isOpen() {
    return !this.closed;
  }
  
  public void close() throws IOException {
    if (!this.closed) {
      Throwable thrown = null;
      
      try {
        if (this.buffer.size > 0L) {
          this.sink.write(this.buffer, this.buffer.size);
        }
      } catch (Throwable var3) {
        thrown = var3;
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
        try {
          Util.sneakyRethrow(thrown);
        } catch (Throwable throwable) {
          throwable.printStackTrace();
        }
      }
      
    }
  }
  
  public Timeout timeout() {
    return this.sink.timeout();
  }
  
  public String toString() {
    return "buffer(" + this.sink + ")";
  }
}
