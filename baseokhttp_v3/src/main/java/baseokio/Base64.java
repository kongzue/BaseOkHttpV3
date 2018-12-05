//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.UnsupportedEncodingException;

final class Base64 {
  private static final byte[] MAP = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
  private static final byte[] URL_MAP = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
  
  private Base64() {
  }
  
  public static byte[] decode(String in) {
    int limit;
    for(limit = in.length(); limit > 0; --limit) {
      char c = in.charAt(limit - 1);
      if (c != '=' && c != '\n' && c != '\r' && c != ' ' && c != '\t') {
        break;
      }
    }
    
    byte[] out = new byte[(int)((long)limit * 6L / 8L)];
    int outCount = 0;
    int inCount = 0;
    int word = 0;
    
    int lastWordChars;
    for(lastWordChars = 0; lastWordChars < limit; ++lastWordChars) {
      char c = in.charAt(lastWordChars);
      int bits;
      if (c >= 'A' && c <= 'Z') {
        bits = c - 65;
      } else if (c >= 'a' && c <= 'z') {
        bits = c - 71;
      } else if (c >= '0' && c <= '9') {
        bits = c + 4;
      } else if (c != '+' && c != '-') {
        if (c != '/' && c != '_') {
          if (c != '\n' && c != '\r' && c != ' ' && c != '\t') {
            return null;
          }
          continue;
        }
        
        bits = 63;
      } else {
        bits = 62;
      }
      
      word = word << 6 | (byte)bits;
      ++inCount;
      if (inCount % 4 == 0) {
        out[outCount++] = (byte)(word >> 16);
        out[outCount++] = (byte)(word >> 8);
        out[outCount++] = (byte)word;
      }
    }
    
    lastWordChars = inCount % 4;
    if (lastWordChars == 1) {
      return null;
    } else {
      if (lastWordChars == 2) {
        word <<= 12;
        out[outCount++] = (byte)(word >> 16);
      } else if (lastWordChars == 3) {
        word <<= 6;
        out[outCount++] = (byte)(word >> 16);
        out[outCount++] = (byte)(word >> 8);
      }
      
      if (outCount == out.length) {
        return out;
      } else {
        byte[] prefix = new byte[outCount];
        System.arraycopy(out, 0, prefix, 0, outCount);
        return prefix;
      }
    }
  }
  
  public static String encode(byte[] in) {
    return encode(in, MAP);
  }
  
  public static String encodeUrl(byte[] in) {
    return encode(in, URL_MAP);
  }
  
  private static String encode(byte[] in, byte[] map) {
    int length = (in.length + 2) / 3 * 4;
    byte[] out = new byte[length];
    int index = 0;
    int end = in.length - in.length % 3;
    
    for(int i = 0; i < end; i += 3) {
      out[index++] = map[(in[i] & 255) >> 2];
      out[index++] = map[(in[i] & 3) << 4 | (in[i + 1] & 255) >> 4];
      out[index++] = map[(in[i + 1] & 15) << 2 | (in[i + 2] & 255) >> 6];
      out[index++] = map[in[i + 2] & 63];
    }
    
    switch(in.length % 3) {
      case 1:
        out[index++] = map[(in[end] & 255) >> 2];
        out[index++] = map[(in[end] & 3) << 4];
        out[index++] = 61;
        out[index++] = 61;
        break;
      case 2:
        out[index++] = map[(in[end] & 255) >> 2];
        out[index++] = map[(in[end] & 3) << 4 | (in[end + 1] & 255) >> 4];
        out[index++] = map[(in[end + 1] & 15) << 2];
        out[index++] = 61;
    }
    
    try {
      return new String(out, "US-ASCII");
    } catch (UnsupportedEncodingException var7) {
      throw new AssertionError(var7);
    }
  }
}
