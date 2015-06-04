package example.cache.string;

public interface StringServiceSync
{
  String get();

  boolean set(String value);

  boolean delete();

  boolean exists();
}
