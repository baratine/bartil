package bartil.string;

import io.baratine.core.ResultStream;

public interface StringServiceSync
{
  String get();

  boolean set(String value);

  boolean delete();

  boolean exists();

  //long watch(ResultStream<String> watcher);

  boolean unwatch(long id);
}
