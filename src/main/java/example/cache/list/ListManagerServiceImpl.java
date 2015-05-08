package example.cache.list;

import io.baratine.core.Journal;
import io.baratine.core.OnLookup;
import io.baratine.core.Lookup;
import io.baratine.core.Service;
import io.baratine.store.Store;

import java.util.LinkedList;

import javax.inject.Inject;

@Journal
@Service("public:///list")
public class ListManagerServiceImpl<T> implements ListManagerService
{
  @Inject @Lookup("store:///list")
  private Store<LinkedList<T>> _store;

  @OnLookup
  public ListServiceImpl<T> onLookup(String url)
  {
    int i = url.indexOf('/', 0);
    String id = url.substring(i + 1);

    String storeKey = "/" + id + "/list";

    ListServiceImpl<T> list = new ListServiceImpl<T>(id, storeKey, _store);

    return list;
  }
}
