//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;


final class RealBufferedSource implements BufferedSource {
  public final Buffer buffer = new Buffer();
  public final Source source;
  boolean closed;
  
  RealBufferedSource(Source source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    } else {
      this.source = source;
    }
  }
  
  public Buffer buffer() {
    return this.buffer;
  }
  
  public long read(Buffer sink, long byteCount) throws IOException {
    if (sink == null) {
      throw new IllegalArgumentException("sink == null");
    } else if (byteCount < 0L) {
      throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    } else if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      long read;
      if (this.buffer.size == 0L) {
        read = this.source.read(this.buffer, 8192L);
        if (read == -1L) {
          return -1L;
        }
      }
      
      read = Math.min(byteCount, this.buffer.size);
      return this.buffer.read(sink, read);
    }
  }
  
  public boolean exhausted() throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      return this.buffer.exhausted() && this.source.read(this.buffer, 8192L) == -1L;
    }
  }
  
  public void require(long byteCount) throws IOException {
    if (!this.request(byteCount)) {
      throw new EOFException();
    }
  }
  
  public boolean request(long byteCount) throws IOException {
    if (byteCount < 0L) {
      throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    } else if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      do {
        if (this.buffer.size >= byteCount) {
          return true;
        }
      } while(this.source.read(this.buffer, 8192L) != -1L);
      
      return false;
    }
  }
  
  public byte readByte() throws IOException {
    this.require(1L);
    return this.buffer.readByte();
  }
  
  public ByteString readByteString() throws IOException {
    this.buffer.writeAll(this.source);
    return this.buffer.readByteString();
  }
  
  public ByteString readByteString(long byteCount) throws IOException {
    this.require(byteCount);
    return this.buffer.readByteString(byteCount);
  }
  
  public int select(Options options) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      do {
        int index = this.buffer.selectPrefix(options);
        if (index == -1) {
          return -1;
        }
        
        int selectedSize = options.byteStrings[index].size();
        if ((long)selectedSize <= this.buffer.size) {
          this.buffer.skip((long)selectedSize);
          return index;
        }
      } while(this.source.read(this.buffer, 8192L) != -1L);
      
      return -1;
    }
  }
  
  public byte[] readByteArray() throws IOException {
    this.buffer.writeAll(this.source);
    return this.buffer.readByteArray();
  }
  
  public byte[] readByteArray(long byteCount) throws IOException {
    this.require(byteCount);
    return this.buffer.readByteArray(byteCount);
  }
  
  public int read(byte[] sink) throws IOException {
    return this.read(sink, 0, sink.length);
  }
  
  public void readFully(byte[] sink) throws IOException {
    try {
      this.require((long)sink.length);
    } catch (EOFException var5) {
      int read;
      for(int offset = 0; this.buffer.size > 0L; offset += read) {
        read = this.buffer.read(sink, offset, (int)this.buffer.size);
        if (read == -1) {
          throw new AssertionError();
        }
      }
      
      throw var5;
    }
    
    this.buffer.readFully(sink);
  }
  
  public int read(byte[] sink, int offset, int byteCount) throws IOException {
    Util.checkOffsetAndCount((long)sink.length, (long)offset, (long)byteCount);
    if (this.buffer.size == 0L) {
      long read = this.source.read(this.buffer, 8192L);
      if (read == -1L) {
        return -1;
      }
    }
    
    int toRead = (int)Math.min((long)byteCount, this.buffer.size);
    return this.buffer.read(sink, offset, toRead);
  }
  
  public int read(ByteBuffer sink) throws IOException {
    if (this.buffer.size == 0L) {
      long read = this.source.read(this.buffer, 8192L);
      if (read == -1L) {
        return -1;
      }
    }
    
    return this.buffer.read(sink);
  }
  
  public void readFully(Buffer sink, long byteCount) throws IOException {
    try {
      this.require(byteCount);
    } catch (EOFException var5) {
      sink.writeAll(this.buffer);
      throw var5;
    }
    
    this.buffer.readFully(sink, byteCount);
  }
  
  public long readAll(Sink sink) throws IOException {
    if (sink == null) {
      throw new IllegalArgumentException("sink == null");
    } else {
      long totalBytesWritten = 0L;
      
      while(this.source.read(this.buffer, 8192L) != -1L) {
        long emitByteCount = this.buffer.completeSegmentByteCount();
        if (emitByteCount > 0L) {
          totalBytesWritten += emitByteCount;
          sink.write(this.buffer, emitByteCount);
        }
      }
      
      if (this.buffer.size() > 0L) {
        totalBytesWritten += this.buffer.size();
        sink.write(this.buffer, this.buffer.size());
      }
      
      return totalBytesWritten;
    }
  }
  
  public String readUtf8() throws IOException {
    this.buffer.writeAll(this.source);
    return this.buffer.readUtf8();
  }
  
  public String readUtf8(long byteCount) throws IOException {
    this.require(byteCount);
    return this.buffer.readUtf8(byteCount);
  }
  
  public String readString(Charset charset) throws IOException {
    if (charset == null) {
      throw new IllegalArgumentException("charset == null");
    } else {
      this.buffer.writeAll(this.source);
      return this.buffer.readString(charset);
    }
  }
  
  public String readString(long byteCount, Charset charset) throws IOException {
    this.require(byteCount);
    if (charset == null) {
      throw new IllegalArgumentException("charset == null");
    } else {
      return this.buffer.readString(byteCount, charset);
    }
  }

  public String readUtf8Line() throws IOException {
    long newline = this.indexOf((byte)10);
    if (newline == -1L) {
      return this.buffer.size != 0L ? this.readUtf8(this.buffer.size) : null;
    } else {
      return this.buffer.readUtf8Line(newline);
    }
  }
  
  public String readUtf8LineStrict() throws IOException {
    return this.readUtf8LineStrict(9223372036854775807L);
  }
  
  public String readUtf8LineStrict(long limit) throws IOException {
    if (limit < 0L) {
      throw new IllegalArgumentException("limit < 0: " + limit);
    } else {
      long scanLength = limit == 9223372036854775807L ? 9223372036854775807L : limit + 1L;
      long newline = this.indexOf((byte)10, 0L, scanLength);
      if (newline != -1L) {
        return this.buffer.readUtf8Line(newline);
      } else if (scanLength < 9223372036854775807L && this.request(scanLength) && this.buffer.getByte(scanLength - 1L) == 13 && this.request(scanLength + 1L) && this.buffer.getByte(scanLength) == 10) {
        return this.buffer.readUtf8Line(scanLength);
      } else {
        Buffer data = new Buffer();
        this.buffer.copyTo(data, 0L, Math.min(32L, this.buffer.size()));
        throw new EOFException("\\n not found: limit=" + Math.min(this.buffer.size(), limit) + " content=" + data.readByteString().hex() + '…');
      }
    }
  }
  
  public int readUtf8CodePoint() throws IOException {
    this.require(1L);
    byte b0 = this.buffer.getByte(0L);
    if ((b0 & 224) == 192) {
      this.require(2L);
    } else if ((b0 & 240) == 224) {
      this.require(3L);
    } else if ((b0 & 248) == 240) {
      this.require(4L);
    }
    
    return this.buffer.readUtf8CodePoint();
  }
  
  public short readShort() throws IOException {
    this.require(2L);
    return this.buffer.readShort();
  }
  
  public short readShortLe() throws IOException {
    this.require(2L);
    return this.buffer.readShortLe();
  }
  
  public int readInt() throws IOException {
    this.require(4L);
    return this.buffer.readInt();
  }
  
  public int readIntLe() throws IOException {
    this.require(4L);
    return this.buffer.readIntLe();
  }
  
  public long readLong() throws IOException {
    this.require(8L);
    return this.buffer.readLong();
  }
  
  public long readLongLe() throws IOException {
    this.require(8L);
    return this.buffer.readLongLe();
  }
  
  public long readDecimalLong() throws IOException {
    this.require(1L);
    
    for(int pos = 0; this.request((long)(pos + 1)); ++pos) {
      byte b = this.buffer.getByte((long)pos);
      if ((b < 48 || b > 57) && (pos != 0 || b != 45)) {
        if (pos == 0) {
          throw new NumberFormatException(String.format("Expected leading [0-9] or '-' character but was %#x", b));
        }
        break;
      }
    }
    
    return this.buffer.readDecimalLong();
  }
  
  public long readHexadecimalUnsignedLong() throws IOException {
    this.require(1L);
    
    for(int pos = 0; this.request((long)(pos + 1)); ++pos) {
      byte b = this.buffer.getByte((long)pos);
      if ((b < 48 || b > 57) && (b < 97 || b > 102) && (b < 65 || b > 70)) {
        if (pos == 0) {
          throw new NumberFormatException(String.format("Expected leading [0-9a-fA-F] character but was %#x", b));
        }
        break;
      }
    }
    
    return this.buffer.readHexadecimalUnsignedLong();
  }
  
  public void skip(long byteCount) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      while(byteCount > 0L) {
        if (this.buffer.size == 0L && this.source.read(this.buffer, 8192L) == -1L) {
          throw new EOFException();
        }
        
        long toSkip = Math.min(byteCount, this.buffer.size());
        this.buffer.skip(toSkip);
        byteCount -= toSkip;
      }
      
    }
  }
  
  public long indexOf(byte b) throws IOException {
    return this.indexOf(b, 0L, 9223372036854775807L);
  }
  
  public long indexOf(byte b, long fromIndex) throws IOException {
    return this.indexOf(b, fromIndex, 9223372036854775807L);
  }
  
  public long indexOf(byte b, long fromIndex, long toIndex) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else if (fromIndex >= 0L && toIndex >= fromIndex) {
      while(fromIndex < toIndex) {
        long result = this.buffer.indexOf(b, fromIndex, toIndex);
        if (result != -1L) {
          return result;
        }
        
        long lastBufferSize = this.buffer.size;
        if (lastBufferSize >= toIndex || this.source.read(this.buffer, 8192L) == -1L) {
          return -1L;
        }
        
        fromIndex = Math.max(fromIndex, lastBufferSize);
      }
      
      return -1L;
    } else {
      throw new IllegalArgumentException(String.format("fromIndex=%s toIndex=%s", fromIndex, toIndex));
    }
  }
  
  public long indexOf(ByteString bytes) throws IOException {
    return this.indexOf(bytes, 0L);
  }
  
  public long indexOf(ByteString bytes, long fromIndex) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      while(true) {
        long result = this.buffer.indexOf(bytes, fromIndex);
        if (result != -1L) {
          return result;
        }
        
        long lastBufferSize = this.buffer.size;
        if (this.source.read(this.buffer, 8192L) == -1L) {
          return -1L;
        }
        
        fromIndex = Math.max(fromIndex, lastBufferSize - (long)bytes.size() + 1L);
      }
    }
  }
  
  public long indexOfElement(ByteString targetBytes) throws IOException {
    return this.indexOfElement(targetBytes, 0L);
  }
  
  public long indexOfElement(ByteString targetBytes, long fromIndex) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else {
      while(true) {
        long result = this.buffer.indexOfElement(targetBytes, fromIndex);
        if (result != -1L) {
          return result;
        }
        
        long lastBufferSize = this.buffer.size;
        if (this.source.read(this.buffer, 8192L) == -1L) {
          return -1L;
        }
        
        fromIndex = Math.max(fromIndex, lastBufferSize);
      }
    }
  }
  
  public boolean rangeEquals(long offset, ByteString bytes) throws IOException {
    return this.rangeEquals(offset, bytes, 0, bytes.size());
  }
  
  public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    } else if (offset >= 0L && bytesOffset >= 0 && byteCount >= 0 && bytes.size() - bytesOffset >= byteCount) {
      for(int i = 0; i < byteCount; ++i) {
        long bufferOffset = offset + (long)i;
        if (!this.request(bufferOffset + 1L)) {
          return false;
        }
        
        if (this.buffer.getByte(bufferOffset) != bytes.getByte(bytesOffset + i)) {
          return false;
        }
      }
      
      return true;
    } else {
      return false;
    }
  }
  
  public InputStream inputStream() {
    return new InputStream() {
      public int read() throws IOException {
        if (RealBufferedSource.this.closed) {
          throw new IOException("closed");
        } else {
          if (RealBufferedSource.this.buffer.size == 0L) {
            long count = RealBufferedSource.this.source.read(RealBufferedSource.this.buffer, 8192L);
            if (count == -1L) {
              return -1;
            }
          }
          
          return RealBufferedSource.this.buffer.readByte() & 255;
        }
      }
      
      public int read(byte[] data, int offset, int byteCount) throws IOException {
        if (RealBufferedSource.this.closed) {
          throw new IOException("closed");
        } else {
          Util.checkOffsetAndCount((long)data.length, (long)offset, (long)byteCount);
          if (RealBufferedSource.this.buffer.size == 0L) {
            long count = RealBufferedSource.this.source.read(RealBufferedSource.this.buffer, 8192L);
            if (count == -1L) {
              return -1;
            }
          }
          
          return RealBufferedSource.this.buffer.read(data, offset, byteCount);
        }
      }
      
      public int available() throws IOException {
        if (RealBufferedSource.this.closed) {
          throw new IOException("closed");
        } else {
          return (int)Math.min(RealBufferedSource.this.buffer.size, 2147483647L);
        }
      }
      
      public void close() throws IOException {
        RealBufferedSource.this.close();
      }
      
      public String toString() {
        return RealBufferedSource.this + ".inputStream()";
      }
    };
  }
  
  public boolean isOpen() {
    return !this.closed;
  }
  
  public void close() throws IOException {
    if (!this.closed) {
      this.closed = true;
      this.source.close();
      this.buffer.clear();
    }
  }
  
  public Timeout timeout() {
    return this.source.timeout();
  }
  
  public String toString() {
    return "buffer(" + this.source + ")";
  }
}
