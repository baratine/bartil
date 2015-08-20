package bartil.map;

import io.baratine.core.Journal;
import io.baratine.core.OnLookup;
import io.baratine.core.Lookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceManager;
import io.baratine.core.ServiceRef;
import io.baratine.store.Store;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;

@Journal
@Service("/_map")
public class MapManagerImpl<K,V> implements MapManager<K,V>
{
  @Inject @Lookup("store:///_map")
  private Store<MyHashMap<K,V>> _store;

  @Override
  public void exists(K key, Result<Boolean> result)
  {
    // XXX:

    // XXX: forced to save
  }

  @Override
  public void find(Function<K,Boolean> matcher, Result<List<K>> result)
  {
    // XXX: scanning

    // XXX: forced to save
  }

  @Override
  @SuppressWarnings("unchecked")
  public void deleteSequentially(Result<Integer> result, K ... keys)
  {
    if (keys.length == 0) {
      result.complete(0);

      return;
    }

    deleteSequentially(keys, 0, 0, 0, result);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void deleteFork(Result<Integer> result, K ... keys)
  {
    if (keys.length == 0) {
      result.complete(0);

      return;
    }

    deleteFork(keys, result);
  }

  private void deleteSequentially(K []keys, int index, int total, int count,
                                  Result<Integer> result)
  {
    total = count;

    if (index < keys.length) {
      ServiceManager manager = ServiceManager.getCurrent();
      MapService<K,V> map = manager.lookup("/map-internal/" + keys[index]).as(MapService.class);

      int newTotal = total;

      map.delete(result.from((newCount, r) -> {
        //deleteSequentially(keys, index + 1, newTotal, newCount, r);
      }));
    }
    else {
      result.complete(total);
    }
  }

  private void deleteFork(K []keys, Result<Integer> result)
  {
    Result<Integer> []results
      = result.fork(keys.length, (countA, countB) -> countA + countB);

    ServiceManager manager = ServiceManager.getCurrent();

    for (int i = 0; i < keys.length; i++) {
      MapService<K,V> map = manager.lookup("/map-internal/" + keys[i]).as(MapService.class);

      //map.delete(results[i]);
    }
  }

  @OnLookup
  public MapServiceImpl<K,V> onLookup(String url)
  {
    String storeKey = url + "/map";

    MapServiceImpl<K,V> map = new MapServiceImpl<K,V>(url, storeKey, _store);

    return map;
  }
}