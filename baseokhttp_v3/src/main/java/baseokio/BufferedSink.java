//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

public interface BufferedSink extends Sink, WritableByteChannel {
  Buffer buffer();

  BufferedSink write(ByteString var1) throws IOException;

  BufferedSink write(byte[] var1) throws IOException;

  BufferedSink write(byte[] var1, int var2, int var3) throws IOException;

  long writeAll(Source var1) throws IOException;

  BufferedSink write(Source var1, long var2) throws IOException;

  BufferedSink writeUtf8(String var1) throws IOException;

  BufferedSink writeUtf8(String var1, int var2, int var3) throws IOException;

  BufferedSink writeUtf8CodePoint(int var1) throws IOException;

  BufferedSink writeString(String var1, Charset var2) throws IOException;

  BufferedSink writeString(String var1, int var2, int var3, Charset var4) throws IOException;

  BufferedSink writeByte(int var1) throws IOException;

  BufferedSink writeShort(int var1) throws IOException;

  BufferedSink writeShortLe(int var1) throws IOException;

  BufferedSink writeInt(int var1) throws IOException;

  BufferedSink writeIntLe(int var1) throws IOException;

  BufferedSink writeLong(long var1) throws IOException;

  BufferedSink writeLongLe(long var1) throws IOException;

  BufferedSink writeDecimalLong(long var1) throws IOException;

  BufferedSink writeHexadecimalUnsignedLong(long var1) throws IOException;

  void flush() throws IOException;

  BufferedSink emit() throws IOException;

  BufferedSink emitCompleteSegments() throws IOException;

  OutputStream outputStream();
}
