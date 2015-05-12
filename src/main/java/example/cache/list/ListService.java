package example.cache.list;

import java.util.List;

import io.baratine.core.Result;

public interface ListService<T>
{
  void get(int index, Result<T> result);
  void getRange(int start, int end, Result<List<T>> result);

  void getAll(Result<List<T>> result);

  void pushHead(T value, Result<Integer> result);
  void pushTail(T value, Result<Integer> result);

  void pushHeadMultiple(Result<Integer> result, T ... values);
  void pushTailMultiple(Result<Integer> result, T ... values);

  void popHead(Result<T> result);
  void popTail(Result<T> result);

  void set(int index, T value, Result<Boolean> result);

  void remove(int index, Result<Integer> result);
  void removeValue(T value, int count, Result<Integer> result);

  void trim(int start, int end, Result<Integer> result);
  void clear(Result<Integer> result);
  void size(Result<Integer> result);

  void delete(Result<Boolean> result);
  void exists(Result<Boolean> result);
}
