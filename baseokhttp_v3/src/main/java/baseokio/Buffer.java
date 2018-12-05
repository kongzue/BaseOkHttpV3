//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class Buffer implements BufferedSource, BufferedSink, Cloneable, ByteChannel {
  private static final byte[] DIGITS = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
  static final int REPLACEMENT_CHARACTER = 65533;
 
  Segment head;
  long size;
  
  public Buffer() {
  }
  
  public long size() {
    return this.size;
  }
  
  public Buffer buffer() {
    return this;
  }
  
  public OutputStream outputStream() {
    return new OutputStream() {
      public void write(int b) {
        Buffer.this.writeByte((byte)b);
      }
      
      public void write(byte[] data, int offset, int byteCount) {
        Buffer.this.write(data, offset, byteCount);
      }
      
      public void flush() {
      }
      
      public void close() {
      }
      
      public String toString() {
        return Buffer.this + ".outputStream()";
      }
    };
  }
  
  public Buffer emitCompleteSegments() {
    return this;
  }
  
  public BufferedSink emit() {
    return this;
  }
  
  public boolean exhausted() {
    return this.size == 0L;
  }
  
  public void require(long byteCount) throws EOFException {
    if (this.size < byteCount) {
      throw new EOFException();
    }
  }
  
  public boolean request(long byteCount) {
    return this.size >= byteCount;
  }
  
  public InputStream inputStream() {
    return new InputStream() {
      public int read() {
        return Buffer.this.size > 0L ? Buffer.this.readByte() & 255 : -1;
      }
      
      public int read(byte[] sink, int offset, int byteCount) {
        return Buffer.this.read(sink, offset, byteCount);
      }
      
      public int available() {
        return (int)Math.min(Buffer.this.size, 2147483647L);
      }
      
      public void close() {
      }
      
      public String toString() {
        return Buffer.this + ".inputStream()";
      }
    };
  }
  
  public Buffer copyTo(OutputStream out) throws IOException {
    return this.copyTo(out, 0L, this.size);
  }
  
  public Buffer copyTo(OutputStream out, long offset, long byteCount) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("out == null");
    } else {
      Util.checkOffsetAndCount(this.size, offset, byteCount);
      if (byteCount == 0L) {
        return this;
      } else {
        Segment s;
        for(s = this.head; offset >= (long)(s.limit - s.pos); s = s.next) {
          offset -= (long)(s.limit - s.pos);
        }
        
        while(byteCount > 0L) {
          int pos = (int)((long)s.pos + offset);
          int toCopy = (int)Math.min((long)(s.limit - pos), byteCount);
          out.write(s.data, pos, toCopy);
          byteCount -= (long)toCopy;
          offset = 0L;
          s = s.next;
        }
        
        return this;
      }
    }
  }
  
  public Buffer copyTo(Buffer out, long offset, long byteCount) {
    if (out == null) {
      throw new IllegalArgumentException("out == null");
    } else {
      Util.checkOffsetAndCount(this.size, offset, byteCount);
      if (byteCount == 0L) {
        return this;
      } else {
        out.size += byteCount;
        
        Segment s;
        for(s = this.head; offset >= (long)(s.limit - s.pos); s = s.next) {
          offset -= (long)(s.limit - s.pos);
        }
        
        while(byteCount > 0L) {
          Segment copy = s.sharedCopy();
          copy.pos = (int)((long)copy.pos + offset);
          copy.limit = Math.min(copy.pos + (int)byteCount, copy.limit);
          if (out.head == null) {
            out.head = copy.next = copy.prev = copy;
          } else {
            out.head.prev.push(copy);
          }
          
          byteCount -= (long)(copy.limit - copy.pos);
          offset = 0L;
          s = s.next;
        }
        
        return this;
      }
    }
  }
  
  public Buffer writeTo(OutputStream out) throws IOException {
    return this.writeTo(out, this.size);
  }
  
  public Buffer writeTo(OutputStream out, long byteCount) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("out == null");
    } else {
      Util.checkOffsetAndCount(this.size, 0L, byteCount);
      Segment s = this.head;
      
      while(byteCount > 0L) {
        int toCopy = (int)Math.min(byteCount, (long)(s.limit - s.pos));
        out.write(s.data, s.pos, toCopy);
        s.pos += toCopy;
        this.size -= (long)toCopy;
        byteCount -= (long)toCopy;
        if (s.pos == s.limit) {
          Segment toRecycle = s;
          this.head = s = s.pop();
          SegmentPool.recycle(toRecycle);
        }
      }
      
      return this;
    }
  }
  
  public Buffer readFrom(InputStream in) throws IOException {
    this.readFrom(in, 9223372036854775807L, true);
    return this;
  }
  
  public Buffer readFrom(InputStream in, long byteCount) throws IOException {
    if (byteCount < 0L) {
      throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    } else {
      this.readFrom(in, byteCount, false);
      return this;
    }
  }
  
  private void readFrom(InputStream in, long byteCount, boolean forever) throws IOException {
    if (in == null) {
      throw new IllegalArgumentException("in == null");
    } else {
      while(byteCount > 0L || forever) {
        Segment tail = this.writableSegment(1);
        int maxToCopy = (int)Math.min(byteCount, (long)(8192 - tail.limit));
        int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
        if (bytesRead == -1) {
          if (forever) {
            return;
          }
          
          throw new EOFException();
        }
        
        tail.limit += bytesRead;
        this.size += (long)bytesRead;
        byteCount -= (long)bytesRead;
      }
      
    }
  }
  
  public long completeSegmentByteCount() {
    long result = this.size;
    if (result == 0L) {
      return 0L;
    } else {
      Segment tail = this.head.prev;
      if (tail.limit < 8192 && tail.owner) {
        result -= (long)(tail.limit - tail.pos);
      }
      
      return result;
    }
  }
  
  public byte readByte() {
    if (this.size == 0L) {
      throw new IllegalStateException("size == 0");
    } else {
      Segment segment = this.head;
      int pos = segment.pos;
      int limit = segment.limit;
      byte[] data = segment.data;
      byte b = data[pos++];
      --this.size;
      if (pos == limit) {
        this.head = segment.pop();
        SegmentPool.recycle(segment);
      } else {
        segment.pos = pos;
      }
      
      return b;
    }
  }
  
  public byte getByte(long pos) {
    Util.checkOffsetAndCount(this.size, pos, 1L);
    Segment s;
    if (this.size - pos > pos) {
      s = this.head;
      
      while(true) {
        int segmentByteCount = s.limit - s.pos;
        if (pos < (long)segmentByteCount) {
          return s.data[s.pos + (int)pos];
        }
        
        pos -= (long)segmentByteCount;
        s = s.next;
      }
    } else {
      pos -= this.size;
      s = this.head.prev;
      
      while(true) {
        pos += (long)(s.limit - s.pos);
        if (pos >= 0L) {
          return s.data[s.pos + (int)pos];
        }
        
        s = s.prev;
      }
    }
  }
  
  public short readShort() {
    if (this.size < 2L) {
      throw new IllegalStateException("size < 2: " + this.size);
    } else {
      Segment segment = this.head;
      int pos = segment.pos;
      int limit = segment.limit;
      if (limit - pos < 2) {
        int s = (this.readByte() & 255) << 8 | this.readByte() & 255;
        return (short)s;
      } else {
        byte[] data = segment.data;
        int s = (data[pos++] & 255) << 8 | data[pos++] & 255;
        this.size -= 2L;
        if (pos == limit) {
          this.head = segment.pop();
          SegmentPool.recycle(segment);
        } else {
          segment.pos = pos;
        }
        
        return (short)s;
      }
    }
  }
  
  public int readInt() {
    if (this.size < 4L) {
      throw new IllegalStateException("size < 4: " + this.size);
    } else {
      Segment segment = this.head;
      int pos = segment.pos;
      int limit = segment.limit;
      if (limit - pos < 4) {
        return (this.readByte() & 255) << 24 | (this.readByte() & 255) << 16 | (this.readByte() & 255) << 8 | this.readByte() & 255;
      } else {
        byte[] data = segment.data;
        int i = (data[pos++] & 255) << 24 | (data[pos++] & 255) << 16 | (data[pos++] & 255) << 8 | data[pos++] & 255;
        this.size -= 4L;
        if (pos == limit) {
          this.head = segment.pop();
          SegmentPool.recycle(segment);
        } else {
          segment.pos = pos;
        }
        
        return i;
      }
    }
  }
  
  public long readLong() {
    if (this.size < 8L) {
      throw new IllegalStateException("size < 8: " + this.size);
    } else {
      Segment segment = this.head;
      int pos = segment.pos;
      int limit = segment.limit;
      if (limit - pos < 8) {
        return ((long)this.readInt() & 4294967295L) << 32 | (long)this.readInt() & 4294967295L;
      } else {
        byte[] data = segment.data;
        long v = ((long)data[pos++] & 255L) << 56 | ((long)data[pos++] & 255L) << 48 | ((long)data[pos++] & 255L) << 40 | ((long)data[pos++] & 255L) << 32 | ((long)data[pos++] & 255L) << 24 | ((long)data[pos++] & 255L) << 16 | ((long)data[pos++] & 255L) << 8 | (long)data[pos++] & 255L;
        this.size -= 8L;
        if (pos == limit) {
          this.head = segment.pop();
          SegmentPool.recycle(segment);
        } else {
          segment.pos = pos;
        }
        
        return v;
      }
    }
  }
  
  public short readShortLe() {
    return Util.reverseBytesShort(this.readShort());
  }
  
  public int readIntLe() {
    return Util.reverseBytesInt(this.readInt());
  }
  
  public long readLongLe() {
    return Util.reverseBytesLong(this.readLong());
  }
  
  public long readDecimalLong() {
    if (this.size == 0L) {
      throw new IllegalStateException("size == 0");
    } else {
      long value = 0L;
      int seen = 0;
      boolean negative = false;
      boolean done = false;
      long overflowZone = -922337203685477580L;
      long overflowDigit = -7L;
      
      while(true) {
        Segment segment = this.head;
        byte[] data = segment.data;
        int pos = segment.pos;
        int limit = segment.limit;
        
        while(true) {
          label72: {
            if (pos < limit) {
              byte b = data[pos];
              if (b >= 48 && b <= 57) {
                int digit = 48 - b;
                if (value < overflowZone || value == overflowZone && (long)digit < overflowDigit) {
                  Buffer buffer = (new Buffer()).writeDecimalLong(value).writeByte(b);
                  if (!negative) {
                    buffer.readByte();
                  }
                  
                  throw new NumberFormatException("Number too large: " + buffer.readUtf8());
                }
                
                value *= 10L;
                value += (long)digit;
                break label72;
              }
              
              if (b == 45 && seen == 0) {
                negative = true;
                --overflowDigit;
                break label72;
              }
              
              if (seen == 0) {
                throw new NumberFormatException("Expected leading [0-9] or '-' character but was 0x" + Integer.toHexString(b));
              }
              
              done = true;
            }
            
            if (pos == limit) {
              this.head = segment.pop();
              SegmentPool.recycle(segment);
            } else {
              segment.pos = pos;
            }
            
            if (!done && this.head != null) {
              break;
            }
            
            this.size -= (long)seen;
            return negative ? value : -value;
          }
          
          ++pos;
          ++seen;
        }
      }
    }
  }
  
  public long readHexadecimalUnsignedLong() {
    if (this.size == 0L) {
      throw new IllegalStateException("size == 0");
    } else {
      long value = 0L;
      int seen = 0;
      boolean done = false;
      
      do {
        Segment segment = this.head;
        byte[] data = segment.data;
        int pos = segment.pos;
        
        int limit;
        for(limit = segment.limit; pos < limit; ++seen) {
          byte b = data[pos];
          int digit;
          if (b >= 48 && b <= 57) {
            digit = b - 48;
          } else if (b >= 97 && b <= 102) {
            digit = b - 97 + 10;
          } else {
            if (b < 65 || b > 70) {
              if (seen == 0) {
                throw new NumberFormatException("Expected leading [0-9a-fA-F] character but was 0x" + Integer.toHexString(b));
              }
              
              done = true;
              break;
            }
            
            digit = b - 65 + 10;
          }
          
          if ((value & -1152921504606846976L) != 0L) {
            Buffer buffer = (new Buffer()).writeHexadecimalUnsignedLong(value).writeByte(b);
            throw new NumberFormatException("Number too large: " + buffer.readUtf8());
          }
          
          value <<= 4;
          value |= (long)digit;
          ++pos;
        }
        
        if (pos == limit) {
          this.head = segment.pop();
          SegmentPool.recycle(segment);
        } else {
          segment.pos = pos;
        }
      } while(!done && this.head != null);
      
      this.size -= (long)seen;
      return value;
    }
  }
  
  public ByteString readByteString() {
    return new ByteString(this.readByteArray());
  }
  
  public ByteString readByteString(long byteCount) throws EOFException {
    return new ByteString(this.readByteArray(byteCount));
  }
  
  public int select(Options options) {
    Segment s = this.head;
    if (s == null) {
      return options.indexOf(ByteString.EMPTY);
    } else {
      ByteString[] byteStrings = options.byteStrings;
      int i = 0;
      
      for(int listSize = byteStrings.length; i < listSize; ++i) {
        ByteString b = byteStrings[i];
        if (this.size >= (long)b.size() && this.rangeEquals(s, s.pos, b, 0, b.size())) {
          try {
            this.skip((long)b.size());
            return i;
          } catch (EOFException var8) {
            throw new AssertionError(var8);
          }
        }
      }
      
      return -1;
    }
  }
  
  int selectPrefix(Options options) {
    Segment s = this.head;
    ByteString[] byteStrings = options.byteStrings;
    int i = 0;
    
    for(int listSize = byteStrings.length; i < listSize; ++i) {
      ByteString b = byteStrings[i];
      int bytesLimit = (int)Math.min(this.size, (long)b.size());
      if (bytesLimit == 0 || this.rangeEquals(s, s.pos, b, 0, bytesLimit)) {
        return i;
      }
    }
    
    return -1;
  }
  
  public void readFully(Buffer sink, long byteCount) throws EOFException {
    if (this.size < byteCount) {
      sink.write(this, this.size);
      throw new EOFException();
    } else {
      sink.write(this, byteCount);
    }
  }
  
  public long readAll(Sink sink) throws IOException {
    long byteCount = this.size;
    if (byteCount > 0L) {
      sink.write(this, byteCount);
    }
    
    return byteCount;
  }
  
  public String readUtf8() {
    try {
      return this.readString(this.size, Util.UTF_8);
    } catch (EOFException var2) {
      throw new AssertionError(var2);
    }
  }
  
  public String readUtf8(long byteCount) throws EOFException {
    return this.readString(byteCount, Util.UTF_8);
  }
  
  public String readString(Charset charset) {
    try {
      return this.readString(this.size, charset);
    } catch (EOFException var3) {
      throw new AssertionError(var3);
    }
  }
  
  public String readString(long byteCount, Charset charset) throws EOFException {
    Util.checkOffsetAndCount(this.size, 0L, byteCount);
    if (charset == null) {
      throw new IllegalArgumentException("charset == null");
    } else if (byteCount > 2147483647L) {
      throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
    } else if (byteCount == 0L) {
      return "";
    } else {
      Segment s = this.head;
      if ((long)s.pos + byteCount > (long)s.limit) {
        return new String(this.readByteArray(byteCount), charset);
      } else {
        String result = new String(s.data, s.pos, (int)byteCount, charset);
        s.pos = (int)((long)s.pos + byteCount);
        this.size -= byteCount;
        if (s.pos == s.limit) {
          this.head = s.pop();
          SegmentPool.recycle(s);
        }
        
        return result;
      }
    }
  }
  
  public String readUtf8Line() throws EOFException {
    long newline = this.indexOf((byte)10);
    if (newline == -1L) {
      return this.size != 0L ? this.readUtf8(this.size) : null;
    } else {
      return this.readUtf8Line(newline);
    }
  }
  
  public String readUtf8LineStrict() throws EOFException {
    return this.readUtf8LineStrict(9223372036854775807L);
  }
  
  public String readUtf8LineStrict(long limit) throws EOFException {
    if (limit < 0L) {
      throw new IllegalArgumentException("limit < 0: " + limit);
    } else {
      long scanLength = limit == 9223372036854775807L ? 9223372036854775807L : limit + 1L;
      long newline = this.indexOf((byte)10, 0L, scanLength);
      if (newline != -1L) {
        return this.readUtf8Line(newline);
      } else if (scanLength < this.size() && this.getByte(scanLength - 1L) == 13 && this.getByte(scanLength) == 10) {
        return this.readUtf8Line(scanLength);
      } else {
        Buffer data = new Buffer();
        this.copyTo(data, 0L, Math.min(32L, this.size()));
        throw new EOFException("\\n not found: limit=" + Math.min(this.size(), limit) + " content=" + data.readByteString().hex() + '…');
      }
    }
  }
  
  String readUtf8Line(long newline) throws EOFException {
    String result;
    if (newline > 0L && this.getByte(newline - 1L) == 13) {
      result = this.readUtf8(newline - 1L);
      this.skip(2L);
      return result;
    } else {
      result = this.readUtf8(newline);
      this.skip(1L);
      return result;
    }
  }
  
  public int readUtf8CodePoint() throws EOFException {
    if (this.size == 0L) {
      throw new EOFException();
    } else {
      byte b0 = this.getByte(0L);
      int codePoint;
      byte byteCount;
      int min;
      if ((b0 & 128) == 0) {
        codePoint = b0 & 127;
        byteCount = 1;
        min = 0;
      } else if ((b0 & 224) == 192) {
        codePoint = b0 & 31;
        byteCount = 2;
        min = 128;
      } else if ((b0 & 240) == 224) {
        codePoint = b0 & 15;
        byteCount = 3;
        min = 2048;
      } else {
        if ((b0 & 248) != 240) {
          this.skip(1L);
          return 65533;
        }
        
        codePoint = b0 & 7;
        byteCount = 4;
        min = 65536;
      }
      
      if (this.size < (long)byteCount) {
        throw new EOFException("size < " + byteCount + ": " + this.size + " (to read code point prefixed 0x" + Integer.toHexString(b0) + ")");
      } else {
        for(int i = 1; i < byteCount; ++i) {
          byte b = this.getByte((long)i);
          if ((b & 192) != 128) {
            this.skip((long)i);
            return 65533;
          }
          
          codePoint <<= 6;
          codePoint |= b & 63;
        }
        
        this.skip((long)byteCount);
        if (codePoint > 1114111) {
          return 65533;
        } else if (codePoint >= 55296 && codePoint <= 57343) {
          return 65533;
        } else if (codePoint < min) {
          return 65533;
        } else {
          return codePoint;
        }
      }
    }
  }
  
  public byte[] readByteArray() {
    try {
      return this.readByteArray(this.size);
    } catch (EOFException var2) {
      throw new AssertionError(var2);
    }
  }
  
  public byte[] readByteArray(long byteCount) throws EOFException {
    Util.checkOffsetAndCount(this.size, 0L, byteCount);
    if (byteCount > 2147483647L) {
      throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
    } else {
      byte[] result = new byte[(int)byteCount];
      this.readFully(result);
      return result;
    }
  }
  
  public int read(byte[] sink) {
    return this.read(sink, 0, sink.length);
  }
  
  public void readFully(byte[] sink) throws EOFException {
    int read;
    for(int offset = 0; offset < sink.length; offset += read) {
      read = this.read(sink, offset, sink.length - offset);
      if (read == -1) {
        throw new EOFException();
      }
    }
    
  }
  
  public int read(byte[] sink, int offset, int byteCount) {
    Util.checkOffsetAndCount((long)sink.length, (long)offset, (long)byteCount);
    Segment s = this.head;
    if (s == null) {
      return -1;
    } else {
      int toCopy = Math.min(byteCount, s.limit - s.pos);
      System.arraycopy(s.data, s.pos, sink, offset, toCopy);
      s.pos += toCopy;
      this.size -= (long)toCopy;
      if (s.pos == s.limit) {
        this.head = s.pop();
        SegmentPool.recycle(s);
      }
      
      return toCopy;
    }
  }
  
  public int read(ByteBuffer sink) throws IOException {
    Segment s = this.head;
    if (s == null) {
      return -1;
    } else {
      int toCopy = Math.min(sink.remaining(), s.limit - s.pos);
      sink.put(s.data, s.pos, toCopy);
      s.pos += toCopy;
      this.size -= (long)toCopy;
      if (s.pos == s.limit) {
        this.head = s.pop();
        SegmentPool.recycle(s);
      }
      
      return toCopy;
    }
  }
  
  public void clear() {
    try {
      this.skip(this.size);
    } catch (EOFException var2) {
      throw new AssertionError(var2);
    }
  }
  
  public void skip(long byteCount) throws EOFException {
    while(byteCount > 0L) {
      if (this.head == null) {
        throw new EOFException();
      }
      
      int toSkip = (int)Math.min(byteCount, (long)(this.head.limit - this.head.pos));
      this.size -= (long)toSkip;
      byteCount -= (long)toSkip;
      this.head.pos += toSkip;
      if (this.head.pos == this.head.limit) {
        Segment toRecycle = this.head;
        this.head = toRecycle.pop();
        SegmentPool.recycle(toRecycle);
      }
    }
    
  }
  
  public Buffer write(ByteString byteString) {
    if (byteString == null) {
      throw new IllegalArgumentException("byteString == null");
    } else {
      byteString.write(this);
      return this;
    }
  }
  
  public Buffer writeUtf8(String string) {
    return this.writeUtf8(string, 0, string.length());
  }
  
  public Buffer writeUtf8(String string, int beginIndex, int endIndex) {
    if (string == null) {
      throw new IllegalArgumentException("string == null");
    } else if (beginIndex < 0) {
      throw new IllegalArgumentException("beginIndex < 0: " + beginIndex);
    } else if (endIndex < beginIndex) {
      throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
    } else if (endIndex > string.length()) {
      throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
    } else {
      int i = beginIndex;
      
      while(true) {
        while(i < endIndex) {
          int c = string.charAt(i);
          if (c < 128) {
            Segment tail = this.writableSegment(1);
            byte[] data = tail.data;
            int segmentOffset = tail.limit - i;
            int runLimit = Math.min(endIndex, 8192 - segmentOffset);
            
            for(data[segmentOffset + i++] = (byte)c; i < runLimit; data[segmentOffset + i++] = (byte)c) {
              c = string.charAt(i);
              if (c >= 128) {
                break;
              }
            }
            
            int runSize = i + segmentOffset - tail.limit;
            tail.limit += runSize;
            this.size += (long)runSize;
          } else if (c < 2048) {
            this.writeByte(c >> 6 | 192);
            this.writeByte(c & 63 | 128);
            ++i;
          } else if (c >= '\ud800' && c <= '\udfff') {
            int low = i + 1 < endIndex ? string.charAt(i + 1) : 0;
            if (c <= '\udbff' && low >= '\udc00' && low <= '\udfff') {
              int codePoint = 65536 + ((c & -55297) << 10 | low & -56321);
              this.writeByte(codePoint >> 18 | 240);
              this.writeByte(codePoint >> 12 & 63 | 128);
              this.writeByte(codePoint >> 6 & 63 | 128);
              this.writeByte(codePoint & 63 | 128);
              i += 2;
            } else {
              this.writeByte(63);
              ++i;
            }
          } else {
            this.writeByte(c >> 12 | 224);
            this.writeByte(c >> 6 & 63 | 128);
            this.writeByte(c & 63 | 128);
            ++i;
          }
        }
        
        return this;
      }
    }
  }
  
  public Buffer writeUtf8CodePoint(int codePoint) {
    if (codePoint < 128) {
      this.writeByte(codePoint);
    } else if (codePoint < 2048) {
      this.writeByte(codePoint >> 6 | 192);
      this.writeByte(codePoint & 63 | 128);
    } else if (codePoint < 65536) {
      if (codePoint >= 55296 && codePoint <= 57343) {
        this.writeByte(63);
      } else {
        this.writeByte(codePoint >> 12 | 224);
        this.writeByte(codePoint >> 6 & 63 | 128);
        this.writeByte(codePoint & 63 | 128);
      }
    } else {
      if (codePoint > 1114111) {
        throw new IllegalArgumentException("Unexpected code point: " + Integer.toHexString(codePoint));
      }
      
      this.writeByte(codePoint >> 18 | 240);
      this.writeByte(codePoint >> 12 & 63 | 128);
      this.writeByte(codePoint >> 6 & 63 | 128);
      this.writeByte(codePoint & 63 | 128);
    }
    
    return this;
  }
  
  public Buffer writeString(String string, Charset charset) {
    return this.writeString(string, 0, string.length(), charset);
  }
  
  public Buffer writeString(String string, int beginIndex, int endIndex, Charset charset) {
    if (string == null) {
      throw new IllegalArgumentException("string == null");
    } else if (beginIndex < 0) {
      throw new IllegalAccessError("beginIndex < 0: " + beginIndex);
    } else if (endIndex < beginIndex) {
      throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
    } else if (endIndex > string.length()) {
      throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
    } else if (charset == null) {
      throw new IllegalArgumentException("charset == null");
    } else if (charset.equals(Util.UTF_8)) {
      return this.writeUtf8(string, beginIndex, endIndex);
    } else {
      byte[] data = string.substring(beginIndex, endIndex).getBytes(charset);
      return this.write(data, 0, data.length);
    }
  }
  
  public Buffer write(byte[] source) {
    if (source == null) {
      throw new IllegalArgumentException("source == null");
    } else {
      return this.write(source, 0, source.length);
    }
  }
  
  public Buffer write(byte[] source, int offset, int byteCount) {
    if (source == null) {
      throw new IllegalArgumentException("source == null");
    } else {
      Util.checkOffsetAndCount((long)source.length, (long)offset, (long)byteCount);
      
      Segment tail;
      int toCopy;
      for(int limit = offset + byteCount; offset < limit; tail.limit += toCopy) {
        tail = this.writableSegment(1);
        toCopy = Math.min(limit - offset, 8192 - tail.limit);
        System.arraycopy(source, offset, tail.data, tail.limit, toCopy);
        offset += toCopy;
      }
      
      this.size += (long)byteCount;
      return this;
    }
  }
  
  public int write(ByteBuffer source) throws IOException {
    if (source == null) {
      throw new IllegalArgumentException("source == null");
    } else {
      int byteCount = source.remaining();
      
      Segment tail;
      int toCopy;
      for(int remaining = byteCount; remaining > 0; tail.limit += toCopy) {
        tail = this.writableSegment(1);
        toCopy = Math.min(remaining, 8192 - tail.limit);
        source.get(tail.data, tail.limit, toCopy);
        remaining -= toCopy;
      }
      
      this.size += (long)byteCount;
      return byteCount;
    }
  }
  
  public long writeAll(Source source) throws IOException {
    if (source == null) {
      throw new IllegalArgumentException("source == null");
    } else {
      long totalBytesRead;
      long readCount;
      for(totalBytesRead = 0L; (readCount = source.read(this, 8192L)) != -1L; totalBytesRead += readCount) {
        ;
      }
      
      return totalBytesRead;
    }
  }
  
  public BufferedSink write(Source source, long byteCount) throws IOException {
    while(byteCount > 0L) {
      long read = source.read(this, byteCount);
      if (read == -1L) {
        throw new EOFException();
      }
      
      byteCount -= read;
    }
    
    return this;
  }
  
  public Buffer writeByte(int b) {
    Segment tail = this.writableSegment(1);
    tail.data[tail.limit++] = (byte)b;
    ++this.size;
    return this;
  }
  
  public Buffer writeShort(int s) {
    Segment tail = this.writableSegment(2);
    byte[] data = tail.data;
    int limit = tail.limit;
    data[limit++] = (byte)(s >>> 8 & 255);
    data[limit++] = (byte)(s & 255);
    tail.limit = limit;
    this.size += 2L;
    return this;
  }
  
  public Buffer writeShortLe(int s) {
    return this.writeShort(Util.reverseBytesShort((short)s));
  }
  
  public Buffer writeInt(int i) {
    Segment tail = this.writableSegment(4);
    byte[] data = tail.data;
    int limit = tail.limit;
    data[limit++] = (byte)(i >>> 24 & 255);
    data[limit++] = (byte)(i >>> 16 & 255);
    data[limit++] = (byte)(i >>> 8 & 255);
    data[limit++] = (byte)(i & 255);
    tail.limit = limit;
    this.size += 4L;
    return this;
  }
  
  public Buffer writeIntLe(int i) {
    return this.writeInt(Util.reverseBytesInt(i));
  }
  
  public Buffer writeLong(long v) {
    Segment tail = this.writableSegment(8);
    byte[] data = tail.data;
    int limit = tail.limit;
    data[limit++] = (byte)((int)(v >>> 56 & 255L));
    data[limit++] = (byte)((int)(v >>> 48 & 255L));
    data[limit++] = (byte)((int)(v >>> 40 & 255L));
    data[limit++] = (byte)((int)(v >>> 32 & 255L));
    data[limit++] = (byte)((int)(v >>> 24 & 255L));
    data[limit++] = (byte)((int)(v >>> 16 & 255L));
    data[limit++] = (byte)((int)(v >>> 8 & 255L));
    data[limit++] = (byte)((int)(v & 255L));
    tail.limit = limit;
    this.size += 8L;
    return this;
  }
  
  public Buffer writeLongLe(long v) {
    return this.writeLong(Util.reverseBytesLong(v));
  }
  
  public Buffer writeDecimalLong(long v) {
    if (v == 0L) {
      return this.writeByte(48);
    } else {
      boolean negative = false;
      if (v < 0L) {
        v = -v;
        if (v < 0L) {
          return this.writeUtf8("-9223372036854775808");
        }
        
        negative = true;
      }
      
      int width = v < 100000000L ? (v < 10000L ? (v < 100L ? (v < 10L ? 1 : 2) : (v < 1000L ? 3 : 4)) : (v < 1000000L ? (v < 100000L ? 5 : 6) : (v < 10000000L ? 7 : 8))) : (v < 1000000000000L ? (v < 10000000000L ? (v < 1000000000L ? 9 : 10) : (v < 100000000000L ? 11 : 12)) : (v < 1000000000000000L ? (v < 10000000000000L ? 13 : (v < 100000000000000L ? 14 : 15)) : (v < 100000000000000000L ? (v < 10000000000000000L ? 16 : 17) : (v < 1000000000000000000L ? 18 : 19))));
      if (negative) {
        ++width;
      }
      
      Segment tail = this.writableSegment(width);
      byte[] data = tail.data;
      
      int pos;
      for(pos = tail.limit + width; v != 0L; v /= 10L) {
        int digit = (int)(v % 10L);
        --pos;
        data[pos] = DIGITS[digit];
      }
      
      if (negative) {
        --pos;
        data[pos] = 45;
      }
      
      tail.limit += width;
      this.size += (long)width;
      return this;
    }
  }
  
  public Buffer writeHexadecimalUnsignedLong(long v) {
    if (v == 0L) {
      return this.writeByte(48);
    } else {
      int width = Long.numberOfTrailingZeros(Long.highestOneBit(v)) / 4 + 1;
      Segment tail = this.writableSegment(width);
      byte[] data = tail.data;
      int pos = tail.limit + width - 1;
      
      for(int start = tail.limit; pos >= start; --pos) {
        data[pos] = DIGITS[(int)(v & 15L)];
        v >>>= 4;
      }
      
      tail.limit += width;
      this.size += (long)width;
      return this;
    }
  }
  
  Segment writableSegment(int minimumCapacity) {
    if (minimumCapacity >= 1 && minimumCapacity <= 8192) {
      if (this.head == null) {
        this.head = SegmentPool.take();
        return this.head.next = this.head.prev = this.head;
      } else {
        Segment tail = this.head.prev;
        if (tail.limit + minimumCapacity > 8192 || !tail.owner) {
          tail = tail.push(SegmentPool.take());
        }
        
        return tail;
      }
    } else {
      throw new IllegalArgumentException();
    }
  }
  
  public void write(Buffer source, long byteCount) {
    if (source == null) {
      throw new IllegalArgumentException("source == null");
    } else if (source == this) {
      throw new IllegalArgumentException("source == this");
    } else {
      Util.checkOffsetAndCount(source.size, 0L, byteCount);
      
      while(byteCount > 0L) {
        Segment segmentToMove;
        if (byteCount < (long)(source.head.limit - source.head.pos)) {
          segmentToMove = this.head != null ? this.head.prev : null;
          if (segmentToMove != null && segmentToMove.owner && byteCount + (long)segmentToMove.limit - (long)(segmentToMove.shared ? 0 : segmentToMove.pos) <= 8192L) {
            source.head.writeTo(segmentToMove, (int)byteCount);
            source.size -= byteCount;
            this.size += byteCount;
            return;
          }
          
          source.head = source.head.split((int)byteCount);
        }
        
        segmentToMove = source.head;
        long movedByteCount = (long)(segmentToMove.limit - segmentToMove.pos);
        source.head = segmentToMove.pop();
        if (this.head == null) {
          this.head = segmentToMove;
          this.head.next = this.head.prev = this.head;
        } else {
          Segment tail = this.head.prev;
          tail = tail.push(segmentToMove);
          tail.compact();
        }
        
        source.size -= movedByteCount;
        this.size += movedByteCount;
        byteCount -= movedByteCount;
      }
      
    }
  }
  
  public long read(Buffer sink, long byteCount) {
    if (sink == null) {
      throw new IllegalArgumentException("sink == null");
    } else if (byteCount < 0L) {
      throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    } else if (this.size == 0L) {
      return -1L;
    } else {
      if (byteCount > this.size) {
        byteCount = this.size;
      }
      
      sink.write(this, byteCount);
      return byteCount;
    }
  }
  
  public long indexOf(byte b) {
    return this.indexOf(b, 0L, 9223372036854775807L);
  }
  
  public long indexOf(byte b, long fromIndex) {
    return this.indexOf(b, fromIndex, 9223372036854775807L);
  }
  
  public long indexOf(byte b, long fromIndex, long toIndex) {
    if (fromIndex >= 0L && toIndex >= fromIndex) {
      if (toIndex > this.size) {
        toIndex = this.size;
      }
      
      if (fromIndex == toIndex) {
        return -1L;
      } else {
        Segment s = this.head;
        if (s == null) {
          return -1L;
        } else {
          long offset;
          long nextOffset;
          if (this.size - fromIndex < fromIndex) {
            for(offset = this.size; offset > fromIndex; offset -= (long)(s.limit - s.pos)) {
              s = s.prev;
            }
          } else {
            for(offset = 0L; (nextOffset = offset + (long)(s.limit - s.pos)) < fromIndex; offset = nextOffset) {
              s = s.next;
            }
          }
          
          while(offset < toIndex) {
            byte[] data = s.data;
            int limit = (int)Math.min((long)s.limit, (long)s.pos + toIndex - offset);
            
            for(int pos = (int)((long)s.pos + fromIndex - offset); pos < limit; ++pos) {
              if (data[pos] == b) {
                return (long)(pos - s.pos) + offset;
              }
            }
            
            offset += (long)(s.limit - s.pos);
            fromIndex = offset;
            s = s.next;
          }
          
          return -1L;
        }
      }
    } else {
      throw new IllegalArgumentException(String.format("size=%s fromIndex=%s toIndex=%s", this.size, fromIndex, toIndex));
    }
  }
  
  public long indexOf(ByteString bytes) throws IOException {
    return this.indexOf(bytes, 0L);
  }
  
  public long indexOf(ByteString bytes, long fromIndex) throws IOException {
    if (bytes.size() == 0) {
      throw new IllegalArgumentException("bytes is empty");
    } else if (fromIndex < 0L) {
      throw new IllegalArgumentException("fromIndex < 0");
    } else {
      Segment s = this.head;
      if (s == null) {
        return -1L;
      } else {
        long offset;
        long nextOffset;
        if (this.size - fromIndex < fromIndex) {
          for(offset = this.size; offset > fromIndex; offset -= (long)(s.limit - s.pos)) {
            s = s.prev;
          }
        } else {
          for(offset = 0L; (nextOffset = offset + (long)(s.limit - s.pos)) < fromIndex; offset = nextOffset) {
            s = s.next;
          }
        }
        
        byte b0 = bytes.getByte(0);
        int bytesSize = bytes.size();
        
        for(long resultLimit = this.size - (long)bytesSize + 1L; offset < resultLimit; s = s.next) {
          byte[] data = s.data;
          int segmentLimit = (int)Math.min((long)s.limit, (long)s.pos + resultLimit - offset);
          
          for(int pos = (int)((long)s.pos + fromIndex - offset); pos < segmentLimit; ++pos) {
            if (data[pos] == b0 && this.rangeEquals(s, pos + 1, bytes, 1, bytesSize)) {
              return (long)(pos - s.pos) + offset;
            }
          }
          
          offset += (long)(s.limit - s.pos);
          fromIndex = offset;
        }
        
        return -1L;
      }
    }
  }
  
  public long indexOfElement(ByteString targetBytes) {
    return this.indexOfElement(targetBytes, 0L);
  }
  
  public long indexOfElement(ByteString targetBytes, long fromIndex) {
    if (fromIndex < 0L) {
      throw new IllegalArgumentException("fromIndex < 0");
    } else {
      Segment s = this.head;
      if (s == null) {
        return -1L;
      } else {
        long offset;
        long nextOffset;
        if (this.size - fromIndex < fromIndex) {
          for(offset = this.size; offset > fromIndex; offset -= (long)(s.limit - s.pos)) {
            s = s.prev;
          }
        } else {
          for(offset = 0L; (nextOffset = offset + (long)(s.limit - s.pos)) < fromIndex; offset = nextOffset) {
            s = s.next;
          }
        }
        
        int pos;
        if (targetBytes.size() != 2) {
          for(byte[] targetByteArray = targetBytes.internalArray(); offset < this.size; s = s.next) {
            byte[] data = s.data;
            pos = (int) ((long) s.pos + fromIndex - offset);
            
            for(pos = s.limit; pos < pos; ++pos) {
              int b = data[pos];
              byte[] var21 = targetByteArray;
              int var13 = targetByteArray.length;
              
              for(int var14 = 0; var14 < var13; ++var14) {
                byte t = var21[var14];
                if (b == t) {
                  return (long)(pos - s.pos) + offset;
                }
              }
            }
            
            offset += (long)(s.limit - s.pos);
            fromIndex = offset;
          }
        } else {
          byte b0 = targetBytes.getByte(0);
          
          for(byte b1 = targetBytes.getByte(1); offset < this.size; s = s.next) {
            byte[] data = s.data;
            pos = (int)((long)s.pos + fromIndex - offset);
            
            for(int limit = s.limit; pos < limit; ++pos) {
              int b = data[pos];
              if (b == b0 || b == b1) {
                return (long)(pos - s.pos) + offset;
              }
            }
            
            offset += (long)(s.limit - s.pos);
            fromIndex = offset;
          }
        }
        
        return -1L;
      }
    }
  }
  
  public boolean rangeEquals(long offset, ByteString bytes) {
    return this.rangeEquals(offset, bytes, 0, bytes.size());
  }
  
  public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) {
    if (offset >= 0L && bytesOffset >= 0 && byteCount >= 0 && this.size - offset >= (long)byteCount && bytes.size() - bytesOffset >= byteCount) {
      for(int i = 0; i < byteCount; ++i) {
        if (this.getByte(offset + (long)i) != bytes.getByte(bytesOffset + i)) {
          return false;
        }
      }
      
      return true;
    } else {
      return false;
    }
  }
  
  private boolean rangeEquals(Segment segment, int segmentPos, ByteString bytes, int bytesOffset, int bytesLimit) {
    int segmentLimit = segment.limit;
    byte[] data = segment.data;
    
    for(int i = bytesOffset; i < bytesLimit; ++i) {
      if (segmentPos == segmentLimit) {
        segment = segment.next;
        data = segment.data;
        segmentPos = segment.pos;
        segmentLimit = segment.limit;
      }
      
      if (data[segmentPos] != bytes.getByte(i)) {
        return false;
      }
      
      ++segmentPos;
    }
    
    return true;
  }
  
  public void flush() {
  }
  
  public boolean isOpen() {
    return true;
  }
  
  public void close() {
  }
  
  public Timeout timeout() {
    return Timeout.NONE;
  }
  
  List<Integer> segmentSizes() {
    if (this.head == null) {
      return Collections.emptyList();
    } else {
      List<Integer> result = new ArrayList();
      result.add(this.head.limit - this.head.pos);
      
      for(Segment s = this.head.next; s != this.head; s = s.next) {
        result.add(s.limit - s.pos);
      }
      
      return result;
    }
  }
  
  public ByteString md5() {
    return this.digest("MD5");
  }
  
  public ByteString sha1() {
    return this.digest("SHA-1");
  }
  
  public ByteString sha256() {
    return this.digest("SHA-256");
  }
  
  public ByteString sha512() {
    return this.digest("SHA-512");
  }
  
  private ByteString digest(String algorithm) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
      if (this.head != null) {
        messageDigest.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
        
        for(Segment s = this.head.next; s != this.head; s = s.next) {
          messageDigest.update(s.data, s.pos, s.limit - s.pos);
        }
      }
      
      return ByteString.of(messageDigest.digest());
    } catch (NoSuchAlgorithmException var4) {
      throw new AssertionError();
    }
  }
  
  public ByteString hmacSha1(ByteString key) {
    return this.hmac("HmacSHA1", key);
  }
  
  public ByteString hmacSha256(ByteString key) {
    return this.hmac("HmacSHA256", key);
  }
  
  public ByteString hmacSha512(ByteString key) {
    return this.hmac("HmacSHA512", key);
  }
  
  private ByteString hmac(String algorithm, ByteString key) {
    try {
      Mac mac = Mac.getInstance(algorithm);
      mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
      if (this.head != null) {
        mac.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
        
        for(Segment s = this.head.next; s != this.head; s = s.next) {
          mac.update(s.data, s.pos, s.limit - s.pos);
        }
      }
      
      return ByteString.of(mac.doFinal());
    } catch (NoSuchAlgorithmException var5) {
      throw new AssertionError();
    } catch (InvalidKeyException var6) {
      throw new IllegalArgumentException(var6);
    }
  }
  
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Buffer)) {
      return false;
    } else {
      Buffer that = (Buffer)o;
      if (this.size != that.size) {
        return false;
      } else if (this.size == 0L) {
        return true;
      } else {
        Segment sa = this.head;
        Segment sb = that.head;
        int posA = sa.pos;
        int posB = sb.pos;
        
        long count;
        for(long pos = 0L; pos < this.size; pos += count) {
          count = (long)Math.min(sa.limit - posA, sb.limit - posB);
          
          for(int i = 0; (long)i < count; ++i) {
            if (sa.data[posA++] != sb.data[posB++]) {
              return false;
            }
          }
          
          if (posA == sa.limit) {
            sa = sa.next;
            posA = sa.pos;
          }
          
          if (posB == sb.limit) {
            sb = sb.next;
            posB = sb.pos;
          }
        }
        
        return true;
      }
    }
  }
  
  public int hashCode() {
    Segment s = this.head;
    if (s == null) {
      return 0;
    } else {
      int result = 1;
      
      do {
        int pos = s.pos;
        
        for(int limit = s.limit; pos < limit; ++pos) {
          result = 31 * result + s.data[pos];
        }
        
        s = s.next;
      } while(s != this.head);
      
      return result;
    }
  }
  
  public String toString() {
    return this.snapshot().toString();
  }
  
  public Buffer clone() {
    Buffer result = new Buffer();
    if (this.size == 0L) {
      return result;
    } else {
      result.head = this.head.sharedCopy();
      result.head.next = result.head.prev = result.head;
      
      for(Segment s = this.head.next; s != this.head; s = s.next) {
        result.head.prev.push(s.sharedCopy());
      }
      
      result.size = this.size;
      return result;
    }
  }
  
  public ByteString snapshot() {
    if (this.size > 2147483647L) {
      throw new IllegalArgumentException("size > Integer.MAX_VALUE: " + this.size);
    } else {
      return this.snapshot((int)this.size);
    }
  }
  
  public ByteString snapshot(int byteCount) {
    return (ByteString)(byteCount == 0 ? ByteString.EMPTY : new SegmentedByteString(this, byteCount));
  }
  
  public Buffer.UnsafeCursor readUnsafe() {
    return this.readUnsafe(new Buffer.UnsafeCursor());
  }
  
  public Buffer.UnsafeCursor readUnsafe(Buffer.UnsafeCursor unsafeCursor) {
    if (unsafeCursor.buffer != null) {
      throw new IllegalStateException("already attached to a buffer");
    } else {
      unsafeCursor.buffer = this;
      unsafeCursor.readWrite = false;
      return unsafeCursor;
    }
  }
  
  public Buffer.UnsafeCursor readAndWriteUnsafe() {
    return this.readAndWriteUnsafe(new Buffer.UnsafeCursor());
  }
  
  public Buffer.UnsafeCursor readAndWriteUnsafe(Buffer.UnsafeCursor unsafeCursor) {
    if (unsafeCursor.buffer != null) {
      throw new IllegalStateException("already attached to a buffer");
    } else {
      unsafeCursor.buffer = this;
      unsafeCursor.readWrite = true;
      return unsafeCursor;
    }
  }
  
  public static final class UnsafeCursor implements Closeable {
    public Buffer buffer;
    public boolean readWrite;
    private Segment segment;
    public long offset = -1L;
    public byte[] data;
    public int start = -1;
    public int end = -1;
    
    public UnsafeCursor() {
    }
    
    public int next() {
      if (this.offset == this.buffer.size) {
        throw new IllegalStateException();
      } else {
        return this.offset == -1L ? this.seek(0L) : this.seek(this.offset + (long)(this.end - this.start));
      }
    }
    
    public int seek(long offset) {
      if (offset >= -1L && offset <= this.buffer.size) {
        if (offset != -1L && offset != this.buffer.size) {
          long min = 0L;
          long max = this.buffer.size;
          Segment head = this.buffer.head;
          Segment tail = this.buffer.head;
          if (this.segment != null) {
            long segmentOffset = this.offset - (long)(this.start - this.segment.pos);
            if (segmentOffset > offset) {
              max = segmentOffset;
              tail = this.segment;
            } else {
              min = segmentOffset;
              head = this.segment;
            }
          }
          
          long nextOffset;
          Segment next;
          if (max - offset > offset - min) {
            next = head;
            
            for(nextOffset = min; offset >= nextOffset + (long)(next.limit - next.pos); next = next.next) {
              nextOffset += (long)(next.limit - next.pos);
            }
          } else {
            next = tail;
            
            for(nextOffset = max; nextOffset > offset; nextOffset -= (long)(next.limit - next.pos)) {
              next = next.prev;
            }
          }
          
          if (this.readWrite && next.shared) {
            Segment unsharedNext = next.unsharedCopy();
            if (this.buffer.head == next) {
              this.buffer.head = unsharedNext;
            }
            
            next = next.push(unsharedNext);
            next.prev.pop();
          }
          
          this.segment = next;
          this.offset = offset;
          this.data = next.data;
          this.start = next.pos + (int)(offset - nextOffset);
          this.end = next.limit;
          return this.end - this.start;
        } else {
          this.segment = null;
          this.offset = offset;
          this.data = null;
          this.start = -1;
          this.end = -1;
          return -1;
        }
      } else {
        throw new ArrayIndexOutOfBoundsException(String.format("offset=%s > size=%s", offset, this.buffer.size));
      }
    }
    
    public long resizeBuffer(long newSize) {
      if (this.buffer == null) {
        throw new IllegalStateException("not attached to a buffer");
      } else if (!this.readWrite) {
        throw new IllegalStateException("resizeBuffer() only permitted for read/write buffers");
      } else {
        long oldSize = this.buffer.size;
        if (newSize <= oldSize) {
          if (newSize < 0L) {
            throw new IllegalArgumentException("newSize < 0: " + newSize);
          }
          
          int tailSize;
          for(long bytesToSubtract = oldSize - newSize; bytesToSubtract > 0L; bytesToSubtract -= (long)tailSize) {
            Segment tail = this.buffer.head.prev;
            tailSize = tail.limit - tail.pos;
            if ((long)tailSize > bytesToSubtract) {
              tail.limit = (int)((long)tail.limit - bytesToSubtract);
              break;
            }
            
            this.buffer.head = tail.pop();
            SegmentPool.recycle(tail);
          }
          
          this.segment = null;
          this.offset = newSize;
          this.data = null;
          this.start = -1;
          this.end = -1;
        } else if (newSize > oldSize) {
          boolean needsToSeek = true;
          long bytesToAdd = newSize - oldSize;
          
          while(bytesToAdd > 0L) {
            Segment tail = this.buffer.writableSegment(1);
            int segmentBytesToAdd = (int)Math.min(bytesToAdd, (long)(8192 - tail.limit));
            tail.limit += segmentBytesToAdd;
            bytesToAdd -= (long)segmentBytesToAdd;
            if (needsToSeek) {
              this.segment = tail;
              this.offset = oldSize;
              this.data = tail.data;
              this.start = tail.limit - segmentBytesToAdd;
              this.end = tail.limit;
              needsToSeek = false;
            }
          }
        }
        
        this.buffer.size = newSize;
        return oldSize;
      }
    }
    
    public long expandBuffer(int minByteCount) {
      if (minByteCount <= 0) {
        throw new IllegalArgumentException("minByteCount <= 0: " + minByteCount);
      } else if (minByteCount > 8192) {
        throw new IllegalArgumentException("minByteCount > Segment.SIZE: " + minByteCount);
      } else if (this.buffer == null) {
        throw new IllegalStateException("not attached to a buffer");
      } else if (!this.readWrite) {
        throw new IllegalStateException("expandBuffer() only permitted for read/write buffers");
      } else {
        long oldSize = this.buffer.size;
        Segment tail = this.buffer.writableSegment(minByteCount);
        int result = 8192 - tail.limit;
        tail.limit = 8192;
        this.buffer.size = oldSize + (long)result;
        this.segment = tail;
        this.offset = oldSize;
        this.data = tail.data;
        this.start = 8192 - result;
        this.end = 8192;
        return (long)result;
      }
    }
    
    public void close() {
      if (this.buffer == null) {
        throw new IllegalStateException("not attached to a buffer");
      } else {
        this.buffer = null;
        this.segment = null;
        this.offset = -1L;
        this.data = null;
        this.start = -1;
        this.end = -1;
      }
    }
  }
}
