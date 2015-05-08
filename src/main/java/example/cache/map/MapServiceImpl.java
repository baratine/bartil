package example.cache.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.store.Store;

public class MapServiceImpl<K,V> implements MapService<K,V>
{
  private static final Logger log
    = Logger.getLogger(MapServiceImpl.class.getName());

  private Store<HashMap<K,V>> _store;

  private String _id;
  private String _storeKey;

  private HashMap<K,V> _map;

  public MapServiceImpl(String id, String storeKey, Store<HashMap<K,V>> store)
  {
    _id = id;
    _storeKey = storeKey;

    _store = store;
  }

  @Override
  public void get(K key, Result<V> result)
  {
    V value = _map.get(key);

    result.complete(value);
  }

  @Override
  public void getKeys(Result<List<K>> result)
  {
    ArrayList<K> list = new ArrayList<>(_map.size());

    Set<K> keySet = _map.keySet();

    for (K key : keySet) {
      list.add(key);
    }

    result.complete(list);
  }

  @Override
  public void getValues(Result<List<V>> result)
  {
    ArrayList<V> list = new ArrayList<>(_map.size());

    Collection<V> valueSet = _map.values();

    for (V value : valueSet) {
      list.add(value);
    }

    result.complete(list);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void getMultiple(Result<List<V>> result, K ... keys)
  {
    ArrayList<V> list = new ArrayList<>(keys.length);

    for (K key : keys) {
      V value = _map.get(key);

      list.add(value);
    }

    result.complete(list);
  }

  @Override
  public void getAll(Result<Map<K,V>> result)
  {
    result.complete(_map);
  }

  @Override
  public void containsKey(K key, Result<Boolean> result)
  {
    result.complete(_map.containsKey(key));
  }

  @Override
  public void containsValue(V value, Result<Boolean> result)
  {
    result.complete(_map.containsValue(value));
  }

  @Modify
  @Override
  public void put(K key, V value, Result<Integer> result)
  {
    _map.put(key, value);

    result.complete(_map.size());
  }

  @Modify
  @Override
  public void putIfAbsent(K key, V value, Result<Boolean> result)
  {
    int size = _map.size();

    _map.putIfAbsent(key, value);

    result.complete(size != _map.size());
  }

  @Modify
  @Override
  public void putMap(Map<K,V> map, Result<Boolean> result)
  {
    _map.putAll(map);

    result.complete(true);
  }

  @Modify
  @Override
  public void remove(K key, Result<Integer> result)
  {
    V value = _map.remove(key);

    result.complete(value != null ? 1 : 0);
  }

  @Modify
  @Override
  @SuppressWarnings("unchecked")
  public void removeMultiple(Result<Integer> result, K ... keys)
  {
    int count = 0;

    for (K key : keys) {
      V value = _map.remove(key);

      if (value != null) {
        count++;
      }
    }

    result.complete(count);
  }

  @Modify
  @Override
  public void rename(K key, K newKey, Result<Boolean> result)
  {
    V value = _map.remove(key);

    _map.put(newKey, value);

    result.complete(true);
  }

  @Override
  public void size(Result<Integer> result)
  {
    result.complete(_map.size());
  }

  @Modify
  @Override
  public void clear(Result<Integer> result)
  {
    int size = _map.size();

    _map.clear();

    result.complete(size);
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

  private boolean onLoadComplete(HashMap<K,V> map)
  {
    if (map != null) {
      _map = map;
    }
    else {
      _map = new HashMap<K,V>();
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

  private Store<HashMap<K,V>> getStore()
  {
    return _store;
  }

  private HashMap<K,V> getMap()
  {
    if (_map == null) {
      _map = new HashMap<>();
    }

    return _map;
  }
}
