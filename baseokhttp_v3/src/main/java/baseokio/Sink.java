//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public interface Sink extends Closeable, Flushable {
  void write(Buffer var1, long var2) throws IOException;

  void flush() throws IOException;

  Timeout timeout();

  void close() throws IOException;
}
