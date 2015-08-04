package bartil.list;

import io.baratine.core.Journal;
import io.baratine.core.OnLookup;
import io.baratine.core.Lookup;
import io.baratine.core.Service;
import io.baratine.store.Store;

import java.util.LinkedList;

import javax.inject.Inject;

@Journal
@Service("/_list")
public class ListManagerServiceImpl<T> implements ListManagerService
{
  @Inject @Lookup("store:///_list")
  private Store<LinkedList<T>> _store;

  @OnLookup
  public ListServiceImpl<T> onLookup(String url)
  {
    String storeKey = url + "/list";

    ListServiceImpl<T> list = new ListServiceImpl<T>(url, storeKey, _store);

    return list;
  }
}
