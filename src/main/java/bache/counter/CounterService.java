package example.cache.counter;

import io.baratine.core.Result;

public interface CounterService
{
  void get(Result<Long> result);

  void set(long value, Result<Boolean> result);

  void increment(long value, Result<Long> result);

  void delete(Result<Boolean> result);

  void exists(Result<Boolean> result);
}
