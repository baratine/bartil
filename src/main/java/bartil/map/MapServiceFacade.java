package bartil.map;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.baratine.core.Lookup;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;

@Service("public:///map")
public class MapServiceFacade<K,V>
{
  @Inject @Lookup("/_map")
  private ServiceRef _service;

  @OnLookup
  public MapServiceFacadeChild<K,V> onLookup(String url)
  {
    MapService<K,V> map = _service.lookup(url).as(MapService.class);

    return new MapServiceFacadeChild<>(map);
  }

  static class MapServiceFacadeChild<K,V> implements MapService<K,V> {
    private MapService<K,V> _map;

    public MapServiceFacadeChild(MapService<K,V> map)
    {
      _map = map;
    }

    public void get(K key, Result<V> result)
    {
      _map.get(key, result);
    }

    public void getKeys(Result<List<K>> result)
    {
      _map.getKeys(result);
    }

    public void getValues(Result<List<V>> result)
    {
      _map.getValues(result);
    }

    public void getMultiple(Result<List<V>> result, K ... keys)
    {
      _map.getMultiple(result, keys);
    }

    public void getAll(Result<Map<K,V>> result)
    {
      _map.getAll(result);
    }

    public void containsKey(K key, Result<Boolean> result)
    {
      _map.containsKey(key, result);
    }

    public void containsValue(V value, Result<Boolean> result)
    {
      _map.containsValue(value, result);
    }

    public void put(K key, V value, Result<Integer> result)
    {
      _map.put(key, value, result);
    }

    public void putIfAbsent(K key, V value, Result<Boolean> result)
    {
      _map.putIfAbsent(key, value, result);
    }

    public void putMap(Map<K,V> map, Result<Integer> result)
    {
      _map.putMap(map, result);
    }

    public void remove(K key, Result<Integer> result)
    {
      _map.remove(key, result);
    }

    public void removeMultiple(Result<Integer> result, K ... keys)
    {
      _map.removeMultiple(result, keys);
    }

    public void rename(K key, K newKey, Result<Boolean> result)
    {
      _map.rename(key, newKey, result);
    }

    public void size(Result<Integer> result)
    {
      _map.size(result);
    }

    public void clear(Result<Integer> result)
    {
      _map.clear(result);
    }

    public void delete(Result<Boolean> result)
    {
      _map.delete(result);
    }

    public void exists(Result<Boolean> result)
    {
      _map.exists(result);
    }
  }
}
