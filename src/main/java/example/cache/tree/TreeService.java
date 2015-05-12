package example.cache.tree;

import java.util.List;
import java.util.Map;

import io.baratine.core.Result;

public interface TreeService<K,V>
{
  void get(K key, Result<V> result);

  void put(K key, V score, Result<Integer> result);

  void getRange(int start, int end, Result<List<Map<K,V>>> result);
  void getRangeDescending(int start, int end, Result<List<Map<K,V>>> result);

  void getRangeKeys(int start, int end, Result<List<K>> result);
  void getRangeDescendingKeys(int start, int end, Result<List<K>> result);

  void size(Result<Integer> result);
  void clear(Result<Integer> result);

  void remove(K key, Result<Integer> result);

  void delete(Result<Boolean> result);
  void exists(Result<Boolean> result);
}
