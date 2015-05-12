package example.cache.list;

import java.util.List;

public interface ListServiceSync<T>
{
  T get(int index);
  List<T> getRange(int start, int end);

  List<T> getAll();

  int pushHead(T value);
  int pushTail(T value);

  int pushHeadMultiple(T ... values);
  int pushTailMultiple(T ... values);

  T popHead();
  T popTail();

  boolean set(int index, T value);

  int remove(int index);
  int removeValue(T value, int count);

  int trim(int start, int end);
  int clear();
  int size();

  boolean delete();
  boolean exists();
}
