//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;


public interface BufferedSource extends Source, ReadableByteChannel {
  Buffer buffer();
  
  boolean exhausted() throws IOException;
  
  void require(long var1) throws IOException;
  
  boolean request(long var1) throws IOException;
  
  byte readByte() throws IOException;
  
  short readShort() throws IOException;
  
  short readShortLe() throws IOException;
  
  int readInt() throws IOException;
  
  int readIntLe() throws IOException;
  
  long readLong() throws IOException;
  
  long readLongLe() throws IOException;
  
  long readDecimalLong() throws IOException;
  
  long readHexadecimalUnsignedLong() throws IOException;
  
  void skip(long var1) throws IOException;
  
  ByteString readByteString() throws IOException;
  
  ByteString readByteString(long var1) throws IOException;
  
  int select(Options var1) throws IOException;
  
  byte[] readByteArray() throws IOException;
  
  byte[] readByteArray(long var1) throws IOException;
  
  int read(byte[] var1) throws IOException;
  
  void readFully(byte[] var1) throws IOException;
  
  int read(byte[] var1, int var2, int var3) throws IOException;
  
  void readFully(Buffer var1, long var2) throws IOException;
  
  long readAll(Sink var1) throws IOException;
  
  String readUtf8() throws IOException;
  
  String readUtf8(long var1) throws IOException;
  
  String readUtf8Line() throws IOException;
  
  String readUtf8LineStrict() throws IOException;
  
  String readUtf8LineStrict(long var1) throws IOException;
  
  int readUtf8CodePoint() throws IOException;
  
  String readString(Charset var1) throws IOException;
  
  String readString(long var1, Charset var3) throws IOException;
  
  long indexOf(byte var1) throws IOException;
  
  long indexOf(byte var1, long var2) throws IOException;
  
  long indexOf(byte var1, long var2, long var4) throws IOException;
  
  long indexOf(ByteString var1) throws IOException;
  
  long indexOf(ByteString var1, long var2) throws IOException;
  
  long indexOfElement(ByteString var1) throws IOException;
  
  long indexOfElement(ByteString var1, long var2) throws IOException;
  
  boolean rangeEquals(long var1, ByteString var3) throws IOException;
  
  boolean rangeEquals(long var1, ByteString var3, int var4, int var5) throws IOException;
  
  InputStream inputStream();
}
