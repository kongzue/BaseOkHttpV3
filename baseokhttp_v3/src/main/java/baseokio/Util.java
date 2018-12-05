//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.nio.charset.Charset;

final class Util {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    
    private Util() {
    }
    
    public static void checkOffsetAndCount(long size, long offset, long byteCount) {
        if ((offset | byteCount) < 0L || offset > size || size - offset < byteCount) {
            throw new ArrayIndexOutOfBoundsException(String.format("size=%s offset=%s byteCount=%s", size, offset, byteCount));
        }
    }
    
    public static short reverseBytesShort(short s) {
        int i = s & '\uffff';
        int reversed = (i & '\uff00') >>> 8 | (i & 255) << 8;
        return (short) reversed;
    }
    
    public static int reverseBytesInt(int i) {
        return (i & -16777216) >>> 24 | (i & 16711680) >>> 8 | (i & '\uff00') << 8 | (i & 255) << 24;
    }
    
    public static long reverseBytesLong(long v) {
        return (v & -72057594037927936L) >>> 56 | (v & 71776119061217280L) >>> 40 | (v & 280375465082880L) >>> 24 | (v & 1095216660480L) >>> 8 | (v & 4278190080L) << 8 | (v & 16711680L) << 24 | (v & 65280L) << 40 | (v & 255L) << 56;
    }
    
    public static void sneakyRethrow(Throwable t) {
        try {
            sneakyThrow2(t);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    private static <T extends Throwable> void sneakyThrow2(Throwable t) throws T {
        try {
            throw t;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    public static boolean arrayRangeEquals(byte[] a, int aOffset, byte[] b, int bOffset, int byteCount) {
        for (int i = 0; i < byteCount; ++i) {
            if (a[i + aOffset] != b[i + bOffset]) {
                return false;
            }
        }
        
        return true;
    }
}
