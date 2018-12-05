//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Okio {
  static final Logger logger = Logger.getLogger(Okio.class.getName());
  
  private Okio() {
  }
  
  public static BufferedSource buffer(Source source) {
    return new RealBufferedSource(source);
  }
  
  public static BufferedSink buffer(Sink sink) {
    return new RealBufferedSink(sink);
  }
  
  public static Sink sink(OutputStream out) {
    return sink(out, new Timeout());
  }
  
  private static Sink sink(final OutputStream out, final Timeout timeout) {
    if (out == null) {
      throw new IllegalArgumentException("out == null");
    } else if (timeout == null) {
      throw new IllegalArgumentException("timeout == null");
    } else {
      return new Sink() {
        public void write(Buffer source, long byteCount) throws IOException {
          Util.checkOffsetAndCount(source.size, 0L, byteCount);
          
          while(byteCount > 0L) {
            timeout.throwIfReached();
            Segment head = source.head;
            int toCopy = (int)Math.min(byteCount, (long)(head.limit - head.pos));
            out.write(head.data, head.pos, toCopy);
            head.pos += toCopy;
            byteCount -= (long)toCopy;
            source.size -= (long)toCopy;
            if (head.pos == head.limit) {
              source.head = head.pop();
              SegmentPool.recycle(head);
            }
          }
          
        }
        
        public void flush() throws IOException {
          out.flush();
        }
        
        public void close() throws IOException {
          out.close();
        }
        
        public Timeout timeout() {
          return timeout;
        }
        
        public String toString() {
          return "sink(" + out + ")";
        }
      };
    }
  }
  
  public static Sink sink(Socket socket) throws IOException {
    if (socket == null) {
      throw new IllegalArgumentException("socket == null");
    } else if (socket.getOutputStream() == null) {
      throw new IOException("socket's output stream == null");
    } else {
      AsyncTimeout timeout = timeout(socket);
      Sink sink = sink((OutputStream)socket.getOutputStream(), (Timeout)timeout);
      return timeout.sink(sink);
    }
  }
  
  public static Source source(InputStream in) {
    return source(in, new Timeout());
  }
  
  private static Source source(final InputStream in, final Timeout timeout) {
    if (in == null) {
      throw new IllegalArgumentException("in == null");
    } else if (timeout == null) {
      throw new IllegalArgumentException("timeout == null");
    } else {
      return new Source() {
        public long read(Buffer sink, long byteCount) throws IOException {
          if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
          } else if (byteCount == 0L) {
            return 0L;
          } else {
            try {
              timeout.throwIfReached();
              Segment tail = sink.writableSegment(1);
              int maxToCopy = (int)Math.min(byteCount, (long)(8192 - tail.limit));
              int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
              if (bytesRead == -1) {
                return -1L;
              } else {
                tail.limit += bytesRead;
                sink.size += (long)bytesRead;
                return (long)bytesRead;
              }
            } catch (AssertionError var7) {
              if (Okio.isAndroidGetsocknameError(var7)) {
                throw new IOException(var7);
              } else {
                throw var7;
              }
            }
          }
        }
        
        public void close() throws IOException {
          in.close();
        }
        
        public Timeout timeout() {
          return timeout;
        }
        
        public String toString() {
          return "source(" + in + ")";
        }
      };
    }
  }
  
  public static Source source(File file) throws FileNotFoundException {
    if (file == null) {
      throw new IllegalArgumentException("file == null");
    } else {
      return source((InputStream)(new FileInputStream(file)));
    }
  }
  
  public static Source source(Path path, OpenOption... options) throws IOException {
    if (path == null) {
      throw new IllegalArgumentException("path == null");
    } else {
      return source(Files.newInputStream(path, options));
    }
  }
  
  public static Sink sink(File file) throws FileNotFoundException {
    if (file == null) {
      throw new IllegalArgumentException("file == null");
    } else {
      return sink((OutputStream)(new FileOutputStream(file)));
    }
  }
  
  public static Sink appendingSink(File file) throws FileNotFoundException {
    if (file == null) {
      throw new IllegalArgumentException("file == null");
    } else {
      return sink((OutputStream)(new FileOutputStream(file, true)));
    }
  }
  
  public static Sink sink(Path path, OpenOption... options) throws IOException {
    if (path == null) {
      throw new IllegalArgumentException("path == null");
    } else {
      return sink(Files.newOutputStream(path, options));
    }
  }
  
  public static Sink blackhole() {
    return new Sink() {
      public void write(Buffer source, long byteCount) throws IOException {
        source.skip(byteCount);
      }
      
      public void flush() throws IOException {
      }
      
      public Timeout timeout() {
        return Timeout.NONE;
      }
      
      public void close() throws IOException {
      }
    };
  }
  
  public static Source source(Socket socket) throws IOException {
    if (socket == null) {
      throw new IllegalArgumentException("socket == null");
    } else if (socket.getInputStream() == null) {
      throw new IOException("socket's input stream == null");
    } else {
      AsyncTimeout timeout = timeout(socket);
      Source source = source((InputStream)socket.getInputStream(), (Timeout)timeout);
      return timeout.source(source);
    }
  }
  
  private static AsyncTimeout timeout(final Socket socket) {
    return new AsyncTimeout() {
      protected IOException newTimeoutException(IOException cause) {
        InterruptedIOException ioe = new SocketTimeoutException("timeout");
        if (cause != null) {
          ioe.initCause(cause);
        }
        
        return ioe;
      }
      
      protected void timedOut() {
        try {
          socket.close();
        } catch (Exception var2) {
          Okio.logger.log(Level.WARNING, "Failed to close timed out socket " + socket, var2);
        } catch (AssertionError var3) {
          if (!Okio.isAndroidGetsocknameError(var3)) {
            throw var3;
          }
          
          Okio.logger.log(Level.WARNING, "Failed to close timed out socket " + socket, var3);
        }
        
      }
    };
  }
  
  static boolean isAndroidGetsocknameError(AssertionError e) {
    return e.getCause() != null && e.getMessage() != null && e.getMessage().contains("getsockname failed");
  }
}
