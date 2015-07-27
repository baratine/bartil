package bartil.tree;

import java.util.List;
import java.util.Map;

public interface TreeServiceSync<K,V>
{
  V get(K key);

  int put(K key, V score);

  List<Map<K,V>> getRange(int start, int end);
  List<Map<K,V>> getRangeDescending(int start, int end);

  List<K> getRangeKeys(int start, int end);
  List<K> getRangeDescendingKeys(int start, int end);

  int size();
  int clear();

  int remove(K key);

  boolean delete();
  boolean exists();
}
