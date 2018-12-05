//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

public class Timeout {
  public static final Timeout NONE = new Timeout() {
    public Timeout timeout(long timeout, TimeUnit unit) {
      return this;
    }
    
    public Timeout deadlineNanoTime(long deadlineNanoTime) {
      return this;
    }
    
    public void throwIfReached() throws IOException {
    }
  };
  private boolean hasDeadline;
  private long deadlineNanoTime;
  private long timeoutNanos;
  
  public Timeout() {
  }
  
  public Timeout timeout(long timeout, TimeUnit unit) {
    if (timeout < 0L) {
      throw new IllegalArgumentException("timeout < 0: " + timeout);
    } else if (unit == null) {
      throw new IllegalArgumentException("unit == null");
    } else {
      this.timeoutNanos = unit.toNanos(timeout);
      return this;
    }
  }
  
  public long timeoutNanos() {
    return this.timeoutNanos;
  }
  
  public boolean hasDeadline() {
    return this.hasDeadline;
  }
  
  public long deadlineNanoTime() {
    if (!this.hasDeadline) {
      throw new IllegalStateException("No deadline");
    } else {
      return this.deadlineNanoTime;
    }
  }
  
  public Timeout deadlineNanoTime(long deadlineNanoTime) {
    this.hasDeadline = true;
    this.deadlineNanoTime = deadlineNanoTime;
    return this;
  }
  
  public final Timeout deadline(long duration, TimeUnit unit) {
    if (duration <= 0L) {
      throw new IllegalArgumentException("duration <= 0: " + duration);
    } else if (unit == null) {
      throw new IllegalArgumentException("unit == null");
    } else {
      return this.deadlineNanoTime(System.nanoTime() + unit.toNanos(duration));
    }
  }
  
  public Timeout clearTimeout() {
    this.timeoutNanos = 0L;
    return this;
  }
  
  public Timeout clearDeadline() {
    this.hasDeadline = false;
    return this;
  }
  
  public void throwIfReached() throws IOException {
    if (Thread.interrupted()) {
      throw new InterruptedIOException("thread interrupted");
    } else if (this.hasDeadline && this.deadlineNanoTime - System.nanoTime() <= 0L) {
      throw new InterruptedIOException("deadline reached");
    }
  }
  
  public final void waitUntilNotified(Object monitor) throws InterruptedIOException {
    try {
      boolean hasDeadline = this.hasDeadline();
      long timeoutNanos = this.timeoutNanos();
      if (!hasDeadline && timeoutNanos == 0L) {
        monitor.wait();
      } else {
        long start = System.nanoTime();
        long waitNanos;
        long elapsedNanos;
        if (hasDeadline && timeoutNanos != 0L) {
          elapsedNanos = this.deadlineNanoTime() - start;
          waitNanos = Math.min(timeoutNanos, elapsedNanos);
        } else if (hasDeadline) {
          waitNanos = this.deadlineNanoTime() - start;
        } else {
          waitNanos = timeoutNanos;
        }
        
        elapsedNanos = 0L;
        if (waitNanos > 0L) {
          long waitMillis = waitNanos / 1000000L;
          monitor.wait(waitMillis, (int)(waitNanos - waitMillis * 1000000L));
          elapsedNanos = System.nanoTime() - start;
        }
        
        if (elapsedNanos >= waitNanos) {
          throw new InterruptedIOException("timeout");
        }
      }
    } catch (InterruptedException var13) {
      throw new InterruptedIOException("interrupted");
    }
  }
}
