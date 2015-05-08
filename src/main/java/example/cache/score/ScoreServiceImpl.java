package example.cache.score;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.store.Store;

public class ScoreServiceImpl<K,V> implements ScoreService<K,V>
{
  private static final Logger log
    = Logger.getLogger(ScoreServiceImpl.class.getName());

  private Store<TreeMap<K,V>> _store;

  private String _id;
  private String _storeKey;
  private TreeMap<K,V> _map;

  public ScoreServiceImpl(String id, String storeKey, Store<TreeMap<K,V>> store)
  {
    _id = id;
    _storeKey = storeKey;

    _store = store;
  }

  @Override
  public void get(K key, Result<V> result)
  {
    result.complete(_map.get(key));
  }

  @Modify
  @Override
  public void put(K key, V value, Result<Integer> result)
  {
    _map.put(key, value);

    result.complete(1);
  }

  @Modify
  @Override
  public void remove(K key, Result<Integer> result)
  {
    _map.remove(key);

    result.complete(1);
  }

  @Override
  public void getRange(int start, int end, Result<List<Map<K,V>>> result)
  {
    LinkedList<Map<K,V>> list = new LinkedList<>();

    if (start < 0) {
      start += _map.size() + 1;
    }

    if (end < 0) {
      end += _map.size() + 1;
    }

    int i = 0;
    for (Map.Entry<K,V> entry : _map.entrySet()) {
      int j = i++;

      if (j < start) {
        continue;
      }
      else if (j >= end) {
        break;
      }
      else {
        HashMap<K,V> map = new HashMap<>();

        map.put(entry.getKey(), entry.getValue());

        list.addLast(map);
      }
    }

    result.complete(list);
  }

  @Override
  public void getRangeDescending(int start, int end, Result<List<Map<K,V>>> result)
  {
    LinkedList<Map<K,V>> list = new LinkedList<>();

    if (start < 0) {
      start += _map.size() + 1;
    }

    if (end < 0) {
      end += _map.size() + 1;
    }

    int i = 0;
    for (Map.Entry<K,V> entry : _map.descendingMap().entrySet()) {
      int j = i++;

      if (j < start) {
        continue;
      }
      else if (j >= end) {
        break;
      }
      else {
        HashMap<K,V> map = new HashMap<>();

        map.put(entry.getKey(), entry.getValue());

        list.addLast(map);
      }
    }

    result.complete(list);
  }

  @Override
  public void getRangeKeys(int start, int end, Result<List<K>> result)
  {
    LinkedList<K> list = new LinkedList<>();

    if (start < 0) {
      start += _map.size() + 1;
    }

    if (end < 0) {
      end += _map.size() + 1;
    }

    int i = 0;
    for (K key : _map.keySet()) {
      int j = i++;

      if (j < start) {
        continue;
      }
      else if (j >= end) {
        break;
      }
      else {
        list.addLast(key);
      }
    }

    result.complete(list);
  }

  @Override
  public void getRangeDescendingKeys(int start, int end, Result<List<K>> result)
  {
    LinkedList<K> list = new LinkedList<>();

    if (start < 0) {
      start += _map.size() + 1;
    }

    if (end < 0) {
      end += _map.size() + 1;
    }

    int i = 0;
    for (K key : _map.descendingKeySet()) {
      int j = i++;

      if (j < start) {
        continue;
      }
      else if (j >= end) {
        break;
      }
      else {
        list.addLast(key);
      }
    }

    result.complete(list);
  }

  @Override
  public void size(Result<Integer> result)
  {
    result.complete(_map.size());
  }

  @Modify
  @Override
  public void clear(Result<Boolean> result)
  {
    _map.clear();

    result.complete(true);
  }

  @Modify
  @Override
  public void delete(Result<Boolean> result)
  {
    _map.clear();

    _map = null;

    result.complete(true);
  }

  @Override
  public void exists(Result<Boolean> result)
  {
    result.complete(_map != null);
  }

  @OnLoad
  public void onLoad(Result<Boolean> result)
  {
    if (log.isLoggable(Level.FINE)) {
      log.fine(getClass().getSimpleName() + ".onLoad0: id=" + _id);
    }

    getStore().get(_storeKey, result.from(v->onLoadComplete(v)));
  }

  private boolean onLoadComplete(TreeMap<K,V> map)
  {
    if (map != null) {
      _map = map;
    }
    else {
      _map = new TreeMap<>();
    }

    if (log.isLoggable(Level.FINE)) {
      log.fine(getClass().getSimpleName() + ".onLoad1: id=" + _id + " done, map=" + map);
    }

    return true;
  }

  @OnSave
  public void onSave(Result<Boolean> result)
  {
    if (log.isLoggable(Level.FINE)) {
      log.fine(getClass().getSimpleName() + ".onSave0: id=" + _id + ", map=" + _map);
    }

    if (_map != null) {
      getStore().put(_storeKey, _map);
    }
    else {
      getStore().remove(_storeKey);
    }

    result.complete(true);

    if (log.isLoggable(Level.FINE)) {
      log.fine(getClass().getSimpleName() + ".onSave1: id=" + _id + " done");
    }
  }

  private Store<TreeMap<K,V>> getStore()
  {
    return _store;
  }
}
