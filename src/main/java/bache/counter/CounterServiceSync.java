package bache.counter;

public interface CounterServiceSync
{
  long get();

  boolean set(long value);

  long increment(long value);

  boolean delete();

  boolean exists();
}
