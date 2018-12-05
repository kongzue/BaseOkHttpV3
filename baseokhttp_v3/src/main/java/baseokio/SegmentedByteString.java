//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

final class SegmentedByteString extends ByteString {
    final transient byte[][] segments;
    final transient int[] directory;

    SegmentedByteString(Buffer buffer, int byteCount) {
        super((byte[])null);
        Util.checkOffsetAndCount(buffer.size, 0L, (long)byteCount);
        int offset = 0;
        int segmentCount = 0;

        Segment s;
        for(s = buffer.head; offset < byteCount; s = s.next) {
            if (s.limit == s.pos) {
                throw new AssertionError("s.limit == s.pos");
            }

            offset += s.limit - s.pos;
            ++segmentCount;
        }

        this.segments = new byte[segmentCount][];
        this.directory = new int[segmentCount * 2];
        offset = 0;
        segmentCount = 0;

        for(s = buffer.head; offset < byteCount; s = s.next) {
            this.segments[segmentCount] = s.data;
            offset += s.limit - s.pos;
            if (offset > byteCount) {
                offset = byteCount;
            }

            this.directory[segmentCount] = offset;
            this.directory[segmentCount + this.segments.length] = s.pos;
            s.shared = true;
            ++segmentCount;
        }

    }

    public String utf8() {
        return this.toByteString().utf8();
    }

    public String string(Charset charset) {
        return this.toByteString().string(charset);
    }

    public String base64() {
        return this.toByteString().base64();
    }

    public String hex() {
        return this.toByteString().hex();
    }

    public ByteString toAsciiLowercase() {
        return this.toByteString().toAsciiLowercase();
    }

    public ByteString toAsciiUppercase() {
        return this.toByteString().toAsciiUppercase();
    }

    public ByteString md5() {
        return this.toByteString().md5();
    }

    public ByteString sha1() {
        return this.toByteString().sha1();
    }

    public ByteString sha256() {
        return this.toByteString().sha256();
    }

    public ByteString hmacSha1(ByteString key) {
        return this.toByteString().hmacSha1(key);
    }

    public ByteString hmacSha256(ByteString key) {
        return this.toByteString().hmacSha256(key);
    }

    public String base64Url() {
        return this.toByteString().base64Url();
    }

    public ByteString substring(int beginIndex) {
        return this.toByteString().substring(beginIndex);
    }

    public ByteString substring(int beginIndex, int endIndex) {
        return this.toByteString().substring(beginIndex, endIndex);
    }

    public byte getByte(int pos) {
        Util.checkOffsetAndCount((long)this.directory[this.segments.length - 1], (long)pos, 1L);
        int segment = this.segment(pos);
        int segmentOffset = segment == 0 ? 0 : this.directory[segment - 1];
        int segmentPos = this.directory[segment + this.segments.length];
        return this.segments[segment][pos - segmentOffset + segmentPos];
    }

    private int segment(int pos) {
        int i = Arrays.binarySearch(this.directory, 0, this.segments.length, pos + 1);
        return i >= 0 ? i : ~i;
    }

    public int size() {
        return this.directory[this.segments.length - 1];
    }

    public byte[] toByteArray() {
        byte[] result = new byte[this.directory[this.segments.length - 1]];
        int segmentOffset = 0;
        int s = 0;

        for(int segmentCount = this.segments.length; s < segmentCount; ++s) {
            int segmentPos = this.directory[segmentCount + s];
            int nextSegmentOffset = this.directory[s];
            System.arraycopy(this.segments[s], segmentPos, result, segmentOffset, nextSegmentOffset - segmentOffset);
            segmentOffset = nextSegmentOffset;
        }

        return result;
    }

    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(this.toByteArray()).asReadOnlyBuffer();
    }

    public void write(OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        } else {
            int segmentOffset = 0;
            int s = 0;

            for(int segmentCount = this.segments.length; s < segmentCount; ++s) {
                int segmentPos = this.directory[segmentCount + s];
                int nextSegmentOffset = this.directory[s];
                out.write(this.segments[s], segmentPos, nextSegmentOffset - segmentOffset);
                segmentOffset = nextSegmentOffset;
            }

        }
    }

    void write(Buffer buffer) {
        int segmentOffset = 0;
        int s = 0;

        for(int segmentCount = this.segments.length; s < segmentCount; ++s) {
            int segmentPos = this.directory[segmentCount + s];
            int nextSegmentOffset = this.directory[s];
            Segment segment = new Segment(this.segments[s], segmentPos, segmentPos + nextSegmentOffset - segmentOffset, true, false);
            if (buffer.head == null) {
                buffer.head = segment.next = segment.prev = segment;
            } else {
                buffer.head.prev.push(segment);
            }

            segmentOffset = nextSegmentOffset;
        }

        buffer.size += (long)segmentOffset;
    }

    public boolean rangeEquals(int offset, ByteString other, int otherOffset, int byteCount) {
        if (offset >= 0 && offset <= this.size() - byteCount) {
            for(int s = this.segment(offset); byteCount > 0; ++s) {
                int segmentOffset = s == 0 ? 0 : this.directory[s - 1];
                int segmentSize = this.directory[s] - segmentOffset;
                int stepSize = Math.min(byteCount, segmentOffset + segmentSize - offset);
                int segmentPos = this.directory[this.segments.length + s];
                int arrayOffset = offset - segmentOffset + segmentPos;
                if (!other.rangeEquals(otherOffset, this.segments[s], arrayOffset, stepSize)) {
                    return false;
                }

                offset += stepSize;
                otherOffset += stepSize;
                byteCount -= stepSize;
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean rangeEquals(int offset, byte[] other, int otherOffset, int byteCount) {
        if (offset >= 0 && offset <= this.size() - byteCount && otherOffset >= 0 && otherOffset <= other.length - byteCount) {
            for(int s = this.segment(offset); byteCount > 0; ++s) {
                int segmentOffset = s == 0 ? 0 : this.directory[s - 1];
                int segmentSize = this.directory[s] - segmentOffset;
                int stepSize = Math.min(byteCount, segmentOffset + segmentSize - offset);
                int segmentPos = this.directory[this.segments.length + s];
                int arrayOffset = offset - segmentOffset + segmentPos;
                if (!Util.arrayRangeEquals(this.segments[s], arrayOffset, other, otherOffset, stepSize)) {
                    return false;
                }

                offset += stepSize;
                otherOffset += stepSize;
                byteCount -= stepSize;
            }

            return true;
        } else {
            return false;
        }
    }

    public int indexOf(byte[] other, int fromIndex) {
        return this.toByteString().indexOf(other, fromIndex);
    }

    public int lastIndexOf(byte[] other, int fromIndex) {
        return this.toByteString().lastIndexOf(other, fromIndex);
    }

    private ByteString toByteString() {
        return new ByteString(this.toByteArray());
    }

    byte[] internalArray() {
        return this.toByteArray();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else {
            return o instanceof ByteString && ((ByteString)o).size() == this.size() && this.rangeEquals(0, (ByteString)((ByteString)o), 0, this.size());
        }
    }

    public int hashCode() {
        int result = this.hashCode;
        if (result != 0) {
            return result;
        } else {
            result = 1;
            int segmentOffset = 0;
            int s = 0;

            for(int segmentCount = this.segments.length; s < segmentCount; ++s) {
                byte[] segment = this.segments[s];
                int segmentPos = this.directory[segmentCount + s];
                int nextSegmentOffset = this.directory[s];
                int segmentSize = nextSegmentOffset - segmentOffset;
                int i = segmentPos;

                for(int limit = segmentPos + segmentSize; i < limit; ++i) {
                    result = 31 * result + segment[i];
                }

                segmentOffset = nextSegmentOffset;
            }

            return this.hashCode = result;
        }
    }

    public String toString() {
        return this.toByteString().toString();
    }

    private Object writeReplace() {
        return this.toByteString();
    }
}
