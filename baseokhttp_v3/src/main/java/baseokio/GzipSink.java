//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public final class GzipSink implements Sink {
  private final BufferedSink sink;
  private final Deflater deflater;
  private final DeflaterSink deflaterSink;
  private boolean closed;
  private final CRC32 crc = new CRC32();
  
  public GzipSink(Sink sink) {
    if (sink == null) {
      throw new IllegalArgumentException("sink == null");
    } else {
      this.deflater = new Deflater(-1, true);
      this.sink = Okio.buffer(sink);
      this.deflaterSink = new DeflaterSink(this.sink, this.deflater);
      this.writeHeader();
    }
  }
  
  public void write(Buffer source, long byteCount) throws IOException {
    if (byteCount < 0L) {
      throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    } else if (byteCount != 0L) {
      this.updateCrc(source, byteCount);
      this.deflaterSink.write(source, byteCount);
    }
  }
  
  public void flush() throws IOException {
    this.deflaterSink.flush();
  }
  
  public Timeout timeout() {
    return this.sink.timeout();
  }
  
  public void close() throws IOException {
    if (!this.closed) {
      Throwable thrown = null;
      
      try {
        this.deflaterSink.finishDeflate();
        this.writeFooter();
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
  
  public Deflater deflater() {
    return this.deflater;
  }
  
  private void writeHeader() {
    Buffer buffer = this.sink.buffer();
    buffer.writeShort(8075);
    buffer.writeByte(8);
    buffer.writeByte(0);
    buffer.writeInt(0);
    buffer.writeByte(0);
    buffer.writeByte(0);
  }
  
  private void writeFooter() throws IOException {
    this.sink.writeIntLe((int)this.crc.getValue());
    this.sink.writeIntLe((int)this.deflater.getBytesRead());
  }
  
  private void updateCrc(Buffer buffer, long byteCount) {
    for(Segment head = buffer.head; byteCount > 0L; head = head.next) {
      int segmentLength = (int)Math.min(byteCount, (long)(head.limit - head.pos));
      this.crc.update(head.data, head.pos, segmentLength);
      byteCount -= (long)segmentLength;
    }
    
  }
}
