package bartil.map;

import java.util.List;
import java.util.Map;

public interface MapServiceSync<K,V>
{
  V get(K key);
  List<K> getKeys();

  List<V> getValues();
  List<V> getMultiple(K ... keys);
  Map<K,V> getAll();

  boolean containsKey(K key);
  boolean containsValue(V value);
  int put(K key, V value);
  boolean putIfAbsent(K key, V value);

  int putMap(Map<K,V> map);
  int remove(K key);
  int removeMultiple(K ... keys);

  boolean rename(K key, K newKey);
  int size();
  int clear();

  boolean delete();
  boolean exists();
}
