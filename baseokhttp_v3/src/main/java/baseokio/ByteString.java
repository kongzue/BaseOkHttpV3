//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ByteString implements Serializable, Comparable<ByteString> {
  static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  private static final long serialVersionUID = 1L;
  public static final ByteString EMPTY = of();
  final byte[] data;
  transient int hashCode;
  transient String utf8;
  
  ByteString(byte[] data) {
    this.data = data;
  }
  
  public static ByteString of(byte... data) {
    if (data == null) {
      throw new IllegalArgumentException("data == null");
    } else {
      return new ByteString((byte[])data.clone());
    }
  }
  
  public static ByteString of(byte[] data, int offset, int byteCount) {
    if (data == null) {
      throw new IllegalArgumentException("data == null");
    } else {
      Util.checkOffsetAndCount((long)data.length, (long)offset, (long)byteCount);
      byte[] copy = new byte[byteCount];
      System.arraycopy(data, offset, copy, 0, byteCount);
      return new ByteString(copy);
    }
  }
  
  public static ByteString of(ByteBuffer data) {
    if (data == null) {
      throw new IllegalArgumentException("data == null");
    } else {
      byte[] copy = new byte[data.remaining()];
      data.get(copy);
      return new ByteString(copy);
    }
  }
  
  public static ByteString encodeUtf8(String s) {
    if (s == null) {
      throw new IllegalArgumentException("s == null");
    } else {
      ByteString byteString = new ByteString(s.getBytes(Util.UTF_8));
      byteString.utf8 = s;
      return byteString;
    }
  }
  
  public static ByteString encodeString(String s, Charset charset) {
    if (s == null) {
      throw new IllegalArgumentException("s == null");
    } else if (charset == null) {
      throw new IllegalArgumentException("charset == null");
    } else {
      return new ByteString(s.getBytes(charset));
    }
  }
  
  public String utf8() {
    String result = this.utf8;
    return result != null ? result : (this.utf8 = new String(this.data, Util.UTF_8));
  }
  
  public String string(Charset charset) {
    if (charset == null) {
      throw new IllegalArgumentException("charset == null");
    } else {
      return new String(this.data, charset);
    }
  }
  
  public String base64() {
    return Base64.encode(this.data);
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
      return of(MessageDigest.getInstance(algorithm).digest(this.data));
    } catch (NoSuchAlgorithmException var3) {
      throw new AssertionError(var3);
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
      return of(mac.doFinal(this.data));
    } catch (NoSuchAlgorithmException var4) {
      throw new AssertionError(var4);
    } catch (InvalidKeyException var5) {
      throw new IllegalArgumentException(var5);
    }
  }
  
  public String base64Url() {
    return Base64.encodeUrl(this.data);
  }
  
  
  public static ByteString decodeBase64(String base64) {
    if (base64 == null) {
      throw new IllegalArgumentException("base64 == null");
    } else {
      byte[] decoded = Base64.decode(base64);
      return decoded != null ? new ByteString(decoded) : null;
    }
  }
  
  public String hex() {
    char[] result = new char[this.data.length * 2];
    int c = 0;
    byte[] var3 = this.data;
    int var4 = var3.length;
    
    for(int var5 = 0; var5 < var4; ++var5) {
      byte b = var3[var5];
      result[c++] = HEX_DIGITS[b >> 4 & 15];
      result[c++] = HEX_DIGITS[b & 15];
    }
    
    return new String(result);
  }
  
  public static ByteString decodeHex(String hex) {
    if (hex == null) {
      throw new IllegalArgumentException("hex == null");
    } else if (hex.length() % 2 != 0) {
      throw new IllegalArgumentException("Unexpected hex string: " + hex);
    } else {
      byte[] result = new byte[hex.length() / 2];
      
      for(int i = 0; i < result.length; ++i) {
        int d1 = decodeHexDigit(hex.charAt(i * 2)) << 4;
        int d2 = decodeHexDigit(hex.charAt(i * 2 + 1));
        result[i] = (byte)(d1 + d2);
      }
      
      return of(result);
    }
  }
  
  private static int decodeHexDigit(char c) {
    if (c >= '0' && c <= '9') {
      return c - 48;
    } else if (c >= 'a' && c <= 'f') {
      return c - 97 + 10;
    } else if (c >= 'A' && c <= 'F') {
      return c - 65 + 10;
    } else {
      throw new IllegalArgumentException("Unexpected hex digit: " + c);
    }
  }
  
  public static ByteString read(InputStream in, int byteCount) throws IOException {
    if (in == null) {
      throw new IllegalArgumentException("in == null");
    } else if (byteCount < 0) {
      throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    } else {
      byte[] result = new byte[byteCount];
      
      int read;
      for(int offset = 0; offset < byteCount; offset += read) {
        read = in.read(result, offset, byteCount - offset);
        if (read == -1) {
          throw new EOFException();
        }
      }
      
      return new ByteString(result);
    }
  }
  
  public ByteString toAsciiLowercase() {
    for(int i = 0; i < this.data.length; ++i) {
      byte c = this.data[i];
      if (c >= 65 && c <= 90) {
        byte[] lowercase = (byte[])this.data.clone();
        
        for(lowercase[i++] = (byte)(c - -32); i < lowercase.length; ++i) {
          c = lowercase[i];
          if (c >= 65 && c <= 90) {
            lowercase[i] = (byte)(c - -32);
          }
        }
        
        return new ByteString(lowercase);
      }
    }
    
    return this;
  }
  
  public ByteString toAsciiUppercase() {
    for(int i = 0; i < this.data.length; ++i) {
      byte c = this.data[i];
      if (c >= 97 && c <= 122) {
        byte[] lowercase = (byte[])this.data.clone();
        
        for(lowercase[i++] = (byte)(c - 32); i < lowercase.length; ++i) {
          c = lowercase[i];
          if (c >= 97 && c <= 122) {
            lowercase[i] = (byte)(c - 32);
          }
        }
        
        return new ByteString(lowercase);
      }
    }
    
    return this;
  }
  
  public ByteString substring(int beginIndex) {
    return this.substring(beginIndex, this.data.length);
  }
  
  public ByteString substring(int beginIndex, int endIndex) {
    if (beginIndex < 0) {
      throw new IllegalArgumentException("beginIndex < 0");
    } else if (endIndex > this.data.length) {
      throw new IllegalArgumentException("endIndex > length(" + this.data.length + ")");
    } else {
      int subLen = endIndex - beginIndex;
      if (subLen < 0) {
        throw new IllegalArgumentException("endIndex < beginIndex");
      } else if (beginIndex == 0 && endIndex == this.data.length) {
        return this;
      } else {
        byte[] copy = new byte[subLen];
        System.arraycopy(this.data, beginIndex, copy, 0, subLen);
        return new ByteString(copy);
      }
    }
  }
  
  public byte getByte(int pos) {
    return this.data[pos];
  }
  
  public int size() {
    return this.data.length;
  }
  
  public byte[] toByteArray() {
    return (byte[])this.data.clone();
  }
  
  byte[] internalArray() {
    return this.data;
  }
  
  public ByteBuffer asByteBuffer() {
    return ByteBuffer.wrap(this.data).asReadOnlyBuffer();
  }
  
  public void write(OutputStream out) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("out == null");
    } else {
      out.write(this.data);
    }
  }
  
  void write(Buffer buffer) {
    buffer.write(this.data, 0, this.data.length);
  }
  
  public boolean rangeEquals(int offset, ByteString other, int otherOffset, int byteCount) {
    return other.rangeEquals(otherOffset, this.data, offset, byteCount);
  }
  
  public boolean rangeEquals(int offset, byte[] other, int otherOffset, int byteCount) {
    return offset >= 0 && offset <= this.data.length - byteCount && otherOffset >= 0 && otherOffset <= other.length - byteCount && Util.arrayRangeEquals(this.data, offset, other, otherOffset, byteCount);
  }
  
  public final boolean startsWith(ByteString prefix) {
    return this.rangeEquals(0, (ByteString)prefix, 0, prefix.size());
  }
  
  public final boolean startsWith(byte[] prefix) {
    return this.rangeEquals(0, (byte[])prefix, 0, prefix.length);
  }
  
  public final boolean endsWith(ByteString suffix) {
    return this.rangeEquals(this.size() - suffix.size(), (ByteString)suffix, 0, suffix.size());
  }
  
  public final boolean endsWith(byte[] suffix) {
    return this.rangeEquals(this.size() - suffix.length, (byte[])suffix, 0, suffix.length);
  }
  
  public final int indexOf(ByteString other) {
    return this.indexOf((byte[])other.internalArray(), 0);
  }
  
  public final int indexOf(ByteString other, int fromIndex) {
    return this.indexOf(other.internalArray(), fromIndex);
  }
  
  public final int indexOf(byte[] other) {
    return this.indexOf((byte[])other, 0);
  }
  
  public int indexOf(byte[] other, int fromIndex) {
    fromIndex = Math.max(fromIndex, 0);
    int i = fromIndex;
    
    for(int limit = this.data.length - other.length; i <= limit; ++i) {
      if (Util.arrayRangeEquals(this.data, i, other, 0, other.length)) {
        return i;
      }
    }
    
    return -1;
  }
  
  public final int lastIndexOf(ByteString other) {
    return this.lastIndexOf(other.internalArray(), this.size());
  }
  
  public final int lastIndexOf(ByteString other, int fromIndex) {
    return this.lastIndexOf(other.internalArray(), fromIndex);
  }
  
  public final int lastIndexOf(byte[] other) {
    return this.lastIndexOf(other, this.size());
  }
  
  public int lastIndexOf(byte[] other, int fromIndex) {
    fromIndex = Math.min(fromIndex, this.data.length - other.length);
    
    for(int i = fromIndex; i >= 0; --i) {
      if (Util.arrayRangeEquals(this.data, i, other, 0, other.length)) {
        return i;
      }
    }
    
    return -1;
  }
  
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else {
      return o instanceof ByteString && ((ByteString)o).size() == this.data.length && ((ByteString)o).rangeEquals(0, (byte[])this.data, 0, this.data.length);
    }
  }
  
  public int hashCode() {
    int result = this.hashCode;
    return result != 0 ? result : (this.hashCode = Arrays.hashCode(this.data));
  }
  
  public int compareTo(ByteString byteString) {
    int sizeA = this.size();
    int sizeB = byteString.size();
    int i = 0;
    
    for(int size = Math.min(sizeA, sizeB); i < size; ++i) {
      int byteA = this.getByte(i) & 255;
      int byteB = byteString.getByte(i) & 255;
      if (byteA != byteB) {
        return byteA < byteB ? -1 : 1;
      }
    }
    
    if (sizeA == sizeB) {
      return 0;
    } else {
      return sizeA < sizeB ? -1 : 1;
    }
  }
  
  public String toString() {
    if (this.data.length == 0) {
      return "[size=0]";
    } else {
      String text = this.utf8();
      int i = codePointIndexToCharIndex(text, 64);
      if (i == -1) {
        return this.data.length <= 64 ? "[hex=" + this.hex() + "]" : "[size=" + this.data.length + " hex=" + this.substring(0, 64).hex() + "…]";
      } else {
        String safeText = text.substring(0, i).replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
        return i < text.length() ? "[size=" + this.data.length + " text=" + safeText + "…]" : "[text=" + safeText + "]";
      }
    }
  }
  
  static int codePointIndexToCharIndex(String s, int codePointCount) {
    int i = 0;
    int j = 0;
    
    int c;
    for(int length = s.length(); i < length; i += Character.charCount(c)) {
      if (j == codePointCount) {
        return i;
      }
      
      c = s.codePointAt(i);
      if (Character.isISOControl(c) && c != 10 && c != 13 || c == 65533) {
        return -1;
      }
      
      ++j;
    }
    
    return s.length();
  }
  
  private void readObject(ObjectInputStream in) throws IOException {
    int dataLength = in.readInt();
    ByteString byteString = read(in, dataLength);
    
    try {
      Field field = ByteString.class.getDeclaredField("data");
      field.setAccessible(true);
      field.set(this, byteString.data);
    } catch (NoSuchFieldException var5) {
      throw new AssertionError();
    } catch (IllegalAccessException var6) {
      throw new AssertionError();
    }
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeInt(this.data.length);
    out.write(this.data);
  }
}
