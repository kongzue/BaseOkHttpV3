//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;


public class AsyncTimeout extends Timeout {
  private static final int TIMEOUT_WRITE_SIZE = 65536;
  private static final long IDLE_TIMEOUT_MILLIS;
  private static final long IDLE_TIMEOUT_NANOS;
  static AsyncTimeout head;
  private boolean inQueue;
  
  private AsyncTimeout next;
  private long timeoutAt;
  
  public AsyncTimeout() {
  }
  
  public final void enter() {
    if (this.inQueue) {
      throw new IllegalStateException("Unbalanced enter/exit");
    } else {
      long timeoutNanos = this.timeoutNanos();
      boolean hasDeadline = this.hasDeadline();
      if (timeoutNanos != 0L || hasDeadline) {
        this.inQueue = true;
        scheduleTimeout(this, timeoutNanos, hasDeadline);
      }
    }
  }
  
  private static synchronized void scheduleTimeout(AsyncTimeout node, long timeoutNanos, boolean hasDeadline) {
    if (head == null) {
      head = new AsyncTimeout();
      (new AsyncTimeout.Watchdog()).start();
    }
    
    long now = System.nanoTime();
    if (timeoutNanos != 0L && hasDeadline) {
      node.timeoutAt = now + Math.min(timeoutNanos, node.deadlineNanoTime() - now);
    } else if (timeoutNanos != 0L) {
      node.timeoutAt = now + timeoutNanos;
    } else {
      if (!hasDeadline) {
        throw new AssertionError();
      }
      
      node.timeoutAt = node.deadlineNanoTime();
    }
    
    long remainingNanos = node.remainingNanos(now);
    
    AsyncTimeout prev;
    for(prev = head; prev.next != null && remainingNanos >= prev.next.remainingNanos(now); prev = prev.next) {
      ;
    }
    
    node.next = prev.next;
    prev.next = node;
    if (prev == head) {
      AsyncTimeout.class.notify();
    }
    
  }
  
  public final boolean exit() {
    if (!this.inQueue) {
      return false;
    } else {
      this.inQueue = false;
      return cancelScheduledTimeout(this);
    }
  }
  
  private static synchronized boolean cancelScheduledTimeout(AsyncTimeout node) {
    for(AsyncTimeout prev = head; prev != null; prev = prev.next) {
      if (prev.next == node) {
        prev.next = node.next;
        node.next = null;
        return false;
      }
    }
    
    return true;
  }
  
  private long remainingNanos(long now) {
    return this.timeoutAt - now;
  }
  
  protected void timedOut() {
  }
  
  public final Sink sink(final Sink sink) {
    return new Sink() {
      public void write(Buffer source, long byteCount) throws IOException {
        Util.checkOffsetAndCount(source.size, 0L, byteCount);
        
        while(byteCount > 0L) {
          long toWrite = 0L;
          
          for(Segment s = source.head; toWrite < 65536L; s = s.next) {
            int segmentSize = s.limit - s.pos;
            toWrite += (long)segmentSize;
            if (toWrite >= byteCount) {
              toWrite = byteCount;
              break;
            }
          }
          
          boolean throwOnTimeout = false;
          AsyncTimeout.this.enter();
          
          try {
            sink.write(source, toWrite);
            byteCount -= toWrite;
            throwOnTimeout = true;
          } catch (IOException var11) {
            throw AsyncTimeout.this.exit(var11);
          } finally {
            AsyncTimeout.this.exit(throwOnTimeout);
          }
        }
        
      }
      
      public void flush() throws IOException {
        boolean throwOnTimeout = false;
        AsyncTimeout.this.enter();
        
        try {
          sink.flush();
          throwOnTimeout = true;
        } catch (IOException var6) {
          throw AsyncTimeout.this.exit(var6);
        } finally {
          AsyncTimeout.this.exit(throwOnTimeout);
        }
        
      }
      
      public void close() throws IOException {
        boolean throwOnTimeout = false;
        AsyncTimeout.this.enter();
        
        try {
          sink.close();
          throwOnTimeout = true;
        } catch (IOException var6) {
          throw AsyncTimeout.this.exit(var6);
        } finally {
          AsyncTimeout.this.exit(throwOnTimeout);
        }
        
      }
      
      public Timeout timeout() {
        return AsyncTimeout.this;
      }
      
      public String toString() {
        return "AsyncTimeout.sink(" + sink + ")";
      }
    };
  }
  
  public final Source source(final Source source) {
    return new Source() {
      public long read(Buffer sink, long byteCount) throws IOException {
        boolean throwOnTimeout = false;
        AsyncTimeout.this.enter();
        
        long var7;
        try {
          long result = source.read(sink, byteCount);
          throwOnTimeout = true;
          var7 = result;
        } catch (IOException var12) {
          throw AsyncTimeout.this.exit(var12);
        } finally {
          AsyncTimeout.this.exit(throwOnTimeout);
        }
        
        return var7;
      }
      
      public void close() throws IOException {
        boolean throwOnTimeout = false;
        
        try {
          source.close();
          throwOnTimeout = true;
        } catch (IOException var6) {
          throw AsyncTimeout.this.exit(var6);
        } finally {
          AsyncTimeout.this.exit(throwOnTimeout);
        }
        
      }
      
      public Timeout timeout() {
        return AsyncTimeout.this;
      }
      
      public String toString() {
        return "AsyncTimeout.source(" + source + ")";
      }
    };
  }
  
  final void exit(boolean throwOnTimeout) throws IOException {
    boolean timedOut = this.exit();
    if (timedOut && throwOnTimeout) {
      throw this.newTimeoutException((IOException)null);
    }
  }
  
  final IOException exit(IOException cause) throws IOException {
    return !this.exit() ? cause : this.newTimeoutException(cause);
  }
  
  protected IOException newTimeoutException(IOException cause) {
    InterruptedIOException e = new InterruptedIOException("timeout");
    if (cause != null) {
      e.initCause(cause);
    }
    
    return e;
  }

  static AsyncTimeout awaitTimeout() throws InterruptedException {
    AsyncTimeout node = head.next;
    long waitNanos;
    if (node != null) {
      waitNanos = node.remainingNanos(System.nanoTime());
      if (waitNanos > 0L) {
        long waitMillis = waitNanos / 1000000L;
        waitNanos -= waitMillis * 1000000L;
        AsyncTimeout.class.wait(waitMillis, (int)waitNanos);
        return null;
      } else {
        head.next = node.next;
        node.next = null;
        return node;
      }
    } else {
      waitNanos = System.nanoTime();
      AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS);
      return head.next == null && System.nanoTime() - waitNanos >= IDLE_TIMEOUT_NANOS ? head : null;
    }
  }
  
  static {
    IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60L);
    IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);
  }
  
  private static final class Watchdog extends Thread {
    Watchdog() {
      super("Okio Watchdog");
      this.setDaemon(true);
    }
    
    public void run() {
      while(true) {
        while(true) {
          try {
            Class var2 = AsyncTimeout.class;
            AsyncTimeout timedOut;
            synchronized(AsyncTimeout.class) {
              timedOut = AsyncTimeout.awaitTimeout();
              if (timedOut == null) {
                continue;
              }
              
              if (timedOut == AsyncTimeout.head) {
                AsyncTimeout.head = null;
                return;
              }
            }
            
            timedOut.timedOut();
          } catch (InterruptedException var5) {
            ;
          }
        }
      }
    }
  }
}
