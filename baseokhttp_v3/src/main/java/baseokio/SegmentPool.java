//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;



final class SegmentPool {
  static final long MAX_SIZE = 65536L;

  static Segment next;
  static long byteCount;

  private SegmentPool() {
  }

  static Segment take() {
    Class var0 = SegmentPool.class;
    synchronized(SegmentPool.class) {
      if (next != null) {
        Segment result = next;
        next = result.next;
        result.next = null;
        byteCount -= 8192L;
        return result;
      }
    }

    return new Segment();
  }

  static void recycle(Segment segment) {
    if (segment.next == null && segment.prev == null) {
      if (!segment.shared) {
        Class var1 = SegmentPool.class;
        synchronized(SegmentPool.class) {
          if (byteCount + 8192L <= 65536L) {
            byteCount += 8192L;
            segment.next = next;
            segment.pos = segment.limit = 0;
            next = segment;
          }
        }
      }
    } else {
      throw new IllegalArgumentException();
    }
  }
}
