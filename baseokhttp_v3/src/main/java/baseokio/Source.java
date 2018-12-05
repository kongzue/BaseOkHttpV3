//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package baseokio;

import java.io.Closeable;
import java.io.IOException;

public interface Source extends Closeable {
  long read(Buffer var1, long var2) throws IOException;

  Timeout timeout();

  void close() throws IOException;
}
