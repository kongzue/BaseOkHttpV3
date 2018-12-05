//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

public final class Utf8 {
    private Utf8() {
    }
    
    public static long size(String string) {
        return size(string, 0, string.length());
    }
    
    public static long size(String string, int beginIndex, int endIndex) {
        if (string == null) {
            throw new IllegalArgumentException("string == null");
        } else if (beginIndex < 0) {
            throw new IllegalArgumentException("beginIndex < 0: " + beginIndex);
        } else if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        } else if (endIndex > string.length()) {
            throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
        } else {
            long result = 0L;
            int i = beginIndex;
            
            while(true) {
                while(i < endIndex) {
                    int c = string.charAt(i);
                    if (c < 128) {
                        ++result;
                        ++i;
                    } else if (c < 2048) {
                        result += 2L;
                        ++i;
                    } else if (c >= '\ud800' && c <= '\udfff') {
                        int low = i + 1 < endIndex ? string.charAt(i + 1) : 0;
                        if (c <= '\udbff' && low >= '\udc00' && low <= '\udfff') {
                            result += 4L;
                            i += 2;
                        } else {
                            ++result;
                            ++i;
                        }
                    } else {
                        result += 3L;
                        ++i;
                    }
                }
                
                return result;
            }
        }
    }
}
