package example.cache.map;

import java.util.List;
import java.util.Map;

import io.baratine.core.Result;

public interface MapService<K,V>
{
  public void get(K key, Result<V> result);
  public void getKeys(Result<List<K>> result);

  public void getValues(Result<List<V>> result);
  public void getMultiple(Result<List<V>> result, K ... keys);
  public void getAll(Result<Map<K,V>> result);
  public void containsKey(K key, Result<Boolean> result);
  public void containsValue(V value, Result<Boolean> result);
  public void put(K key, V value, Result<Integer> result);
  public void putIfAbsent(K key, V value, Result<Boolean> result);

  public void putMap(Map<K,V> map, Result<Boolean> result);
  public void remove(K key, Result<Integer> result);
  public void removeMultiple(Result<Integer> result, K ... keys);
  public void delete(Result<Boolean> result);
  public void exists(Result<Boolean> result);

  public void rename(K key, K newKey, Result<Boolean> result);
  public void size(Result<Integer> result);
  public void clear(Result<Integer> result);

}
