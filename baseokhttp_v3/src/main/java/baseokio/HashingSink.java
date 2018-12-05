//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HashingSink extends ForwardingSink {
    
    private final MessageDigest messageDigest;
    
    private final Mac mac;
    
    public static HashingSink md5(Sink sink) {
        return new HashingSink(sink, "MD5");
    }
    
    public static HashingSink sha1(Sink sink) {
        return new HashingSink(sink, "SHA-1");
    }
    
    public static HashingSink sha256(Sink sink) {
        return new HashingSink(sink, "SHA-256");
    }
    
    public static HashingSink sha512(Sink sink) {
        return new HashingSink(sink, "SHA-512");
    }
    
    public static HashingSink hmacSha1(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA1");
    }
    
    public static HashingSink hmacSha256(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA256");
    }
    
    public static HashingSink hmacSha512(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA512");
    }
    
    private HashingSink(Sink sink, String algorithm) {
        super(sink);
        
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm);
            this.mac = null;
        } catch (NoSuchAlgorithmException var4) {
            throw new AssertionError();
        }
    }
    
    private HashingSink(Sink sink, ByteString key, String algorithm) {
        super(sink);
        
        try {
            this.mac = Mac.getInstance(algorithm);
            this.mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
            this.messageDigest = null;
        } catch (NoSuchAlgorithmException var5) {
            throw new AssertionError();
        } catch (InvalidKeyException var6) {
            throw new IllegalArgumentException(var6);
        }
    }
    
    public void write(Buffer source, long byteCount) throws IOException {
        Util.checkOffsetAndCount(source.size, 0L, byteCount);
        long hashedCount = 0L;
        
        for(Segment s = source.head; hashedCount < byteCount; s = s.next) {
            int toHash = (int)Math.min(byteCount - hashedCount, (long)(s.limit - s.pos));
            if (this.messageDigest != null) {
                this.messageDigest.update(s.data, s.pos, toHash);
            } else {
                this.mac.update(s.data, s.pos, toHash);
            }
            
            hashedCount += (long)toHash;
        }
        
        super.write(source, byteCount);
    }
    
    public ByteString hash() {
        byte[] result = this.messageDigest != null ? this.messageDigest.digest() : this.mac.doFinal();
        return ByteString.of(result);
    }
}
