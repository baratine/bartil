package bartil.map;

import java.util.List;
import java.util.Map;

import io.baratine.core.Result;

public interface MapService<K,V>
{
  void get(K key, Result<V> result);
  void getKeys(Result<List<K>> result);

  void getValues(Result<List<V>> result);
  void getMultiple(Result<List<V>> result, K ... keys);
  void getAll(Result<Map<K,V>> result);

  void containsKey(K key, Result<Boolean> result);
  void containsValue(V value, Result<Boolean> result);
  void put(K key, V value, Result<Integer> result);
  void putIfAbsent(K key, V value, Result<Boolean> result);

  void putMap(Map<K,V> map, Result<Integer> result);
  void remove(K key, Result<Integer> result);
  void removeMultiple(Result<Integer> result, K ... keys);

  void rename(K key, K newKey, Result<Boolean> result);

  void size(Result<Integer> result);
  void clear(Result<Integer> result);

  void delete(Result<Boolean> result);
  void exists(Result<Boolean> result);
}
