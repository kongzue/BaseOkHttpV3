//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Inflater;

public final class GzipSource implements Source {
  private static final byte FHCRC = 1;
  private static final byte FEXTRA = 2;
  private static final byte FNAME = 3;
  private static final byte FCOMMENT = 4;
  private static final byte SECTION_HEADER = 0;
  private static final byte SECTION_BODY = 1;
  private static final byte SECTION_TRAILER = 2;
  private static final byte SECTION_DONE = 3;
  private int section = 0;
  private final BufferedSource source;
  private final Inflater inflater;
  private final InflaterSource inflaterSource;
  private final CRC32 crc = new CRC32();
  
  public GzipSource(Source source) {
    if (source == null) {
      throw new IllegalArgumentException("source == null");
    } else {
      this.inflater = new Inflater(true);
      this.source = Okio.buffer(source);
      this.inflaterSource = new InflaterSource(this.source, this.inflater);
    }
  }
  
  public long read(Buffer sink, long byteCount) throws IOException {
    if (byteCount < 0L) {
      throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    } else if (byteCount == 0L) {
      return 0L;
    } else {
      if (this.section == 0) {
        this.consumeHeader();
        this.section = 1;
      }
      
      if (this.section == 1) {
        long offset = sink.size;
        long result = this.inflaterSource.read(sink, byteCount);
        if (result != -1L) {
          this.updateCrc(sink, offset, result);
          return result;
        }
        
        this.section = 2;
      }
      
      if (this.section == 2) {
        this.consumeTrailer();
        this.section = 3;
        if (!this.source.exhausted()) {
          throw new IOException("gzip finished without exhausting source");
        }
      }
      
      return -1L;
    }
  }
  
  private void consumeHeader() throws IOException {
    this.source.require(10L);
    byte flags = this.source.buffer().getByte(3L);
    boolean fhcrc = (flags >> 1 & 1) == 1;
    if (fhcrc) {
      this.updateCrc(this.source.buffer(), 0L, 10L);
    }
    
    short id1id2 = this.source.readShort();
    this.checkEqual("ID1ID2", 8075, id1id2);
    this.source.skip(8L);
    if ((flags >> 2 & 1) == 1) {
      this.source.require(2L);
      if (fhcrc) {
        this.updateCrc(this.source.buffer(), 0L, 2L);
      }
      
      int xlen = this.source.buffer().readShortLe();
      this.source.require((long)xlen);
      if (fhcrc) {
        this.updateCrc(this.source.buffer(), 0L, (long)xlen);
      }
      
      this.source.skip((long)xlen);
    }
    
    long index;
    if ((flags >> 3 & 1) == 1) {
      index = this.source.indexOf((byte)0);
      if (index == -1L) {
        throw new EOFException();
      }
      
      if (fhcrc) {
        this.updateCrc(this.source.buffer(), 0L, index + 1L);
      }
      
      this.source.skip(index + 1L);
    }
    
    if ((flags >> 4 & 1) == 1) {
      index = this.source.indexOf((byte)0);
      if (index == -1L) {
        throw new EOFException();
      }
      
      if (fhcrc) {
        this.updateCrc(this.source.buffer(), 0L, index + 1L);
      }
      
      this.source.skip(index + 1L);
    }
    
    if (fhcrc) {
      this.checkEqual("FHCRC", this.source.readShortLe(), (short)((int)this.crc.getValue()));
      this.crc.reset();
    }
    
  }
  
  private void consumeTrailer() throws IOException {
    this.checkEqual("CRC", this.source.readIntLe(), (int)this.crc.getValue());
    this.checkEqual("ISIZE", this.source.readIntLe(), (int)this.inflater.getBytesWritten());
  }
  
  public Timeout timeout() {
    return this.source.timeout();
  }
  
  public void close() throws IOException {
    this.inflaterSource.close();
  }
  
  private void updateCrc(Buffer buffer, long offset, long byteCount) {
    Segment s;
    for(s = buffer.head; offset >= (long)(s.limit - s.pos); s = s.next) {
      offset -= (long)(s.limit - s.pos);
    }
    
    while(byteCount > 0L) {
      int pos = (int)((long)s.pos + offset);
      int toUpdate = (int)Math.min((long)(s.limit - pos), byteCount);
      this.crc.update(s.data, pos, toUpdate);
      byteCount -= (long)toUpdate;
      offset = 0L;
      s = s.next;
    }
    
  }
  
  private void checkEqual(String name, int expected, int actual) throws IOException {
    if (actual != expected) {
      throw new IOException(String.format("%s: actual 0x%08x != expected 0x%08x", name, actual, expected));
    }
  }
}
