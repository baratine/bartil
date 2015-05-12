package example.cache.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.store.Store;

public class TreeServiceImpl<K,V> implements TreeService<K,V>
{
  private static final Logger log
    = Logger.getLogger(TreeServiceImpl.class.getName());

  private Store<TreeMap<K,V>> _store;

  private String _id;
  private String _storeKey;
  private TreeMap<K,V> _map;

  public TreeServiceImpl(String id, String storeKey, Store<TreeMap<K,V>> store)
  {
    _id = id;
    _storeKey = storeKey;

    _store = store;
  }

  @Override
  public void get(K key, Result<V> result)
  {
    V value = null;

    if (_map != null) {
      value = _map.get(key);
    }

    result.complete(value);
  }

  @Modify
  @Override
  public void put(K key, V value, Result<Integer> result)
  {
    getMap().put(key, value);

    result.complete(getMap().size());
  }

  @Modify
  @Override
  public void remove(K key, Result<Integer> result)
  {
    int removeCount = 0;

    if (_map != null && _map.containsKey(key)) {
      _map.remove(key);

      removeCount = 1;
    }

    result.complete(removeCount);
  }

  @Override
  public void getRange(int start, int end, Result<List<Map<K,V>>> result)
  {
    LinkedList<Map<K,V>> list = new LinkedList<>();

    getRange(start, end, list, false);

    result.complete(list);
  }

  @Override
  public void getRangeDescending(int start, int end, Result<List<Map<K,V>>> result)
  {
    LinkedList<Map<K,V>> list = new LinkedList<>();

    getRange(start, end, list, true);

    result.complete(list);
  }

  private void getRange(int start, int end, LinkedList<Map<K,V>> list, boolean isDescending)
  {
    if (_map != null) {
      if (start < 0) {
        start += _map.size() + 1;
      }

      if (end < 0) {
        end += _map.size() + 1;
      }

      int i = 0;
      Set<Map.Entry<K,V>> set;

      if (isDescending) {
        set = _map.descendingMap().entrySet();
      }
      else {
        set = _map.entrySet();
      }

      for (Map.Entry<K,V> entry : set) {
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
    }
  }

  @Override
  public void getRangeKeys(int start, int end, Result<List<K>> result)
  {
    LinkedList<K> list = new LinkedList<>();

    getRangeKeys(start, end, list, false);

    result.complete(list);
  }

  @Override
  public void getRangeDescendingKeys(int start, int end, Result<List<K>> result)
  {
    LinkedList<K> list = new LinkedList<>();

    getRangeKeys(start, end, list, true);

    result.complete(list);
  }

  private void getRangeKeys(int start, int end, LinkedList<K> list, boolean isDescending)
  {
    if (_map != null) {
      if (start < 0) {
        start += _map.size() + 1;
      }

      if (end < 0) {
        end += _map.size() + 1;
      }

      int i = 0;

      Set<K> set;

      if (isDescending) {
        set = _map.descendingKeySet();
      }
      else {
        set = _map.keySet();
      }

      for (K key : set) {
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
    }
  }

  @Override
  public void size(Result<Integer> result)
  {
    int size = -1;

    if (_map != null) {
      size = _map.size();
    }

    result.complete(size);
  }

  @Modify
  @Override
  public void clear(Result<Integer> result)
  {
    int size = -1;

    if (_map != null) {
      size = _map.size();

      _map.clear();
    }

    result.complete(size);
  }

  @Modify
  @Override
  public void delete(Result<Boolean> result)
  {
    boolean isDeleted = false;

    if (_map != null) {
      _map.clear();

      _map = null;

      isDeleted = true;
    }

    result.complete(isDeleted);
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
      _map = null;
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

  private TreeMap<K,V> getMap()
  {
    if (_map == null) {
      _map = new TreeMap<>();
    }

    return _map;
  }

  private Store<TreeMap<K,V>> getStore()
  {
    return _store;
  }
}
