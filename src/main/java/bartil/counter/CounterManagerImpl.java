package bartil.counter;

import javax.inject.Inject;

import io.baratine.core.Journal;
import io.baratine.core.Lookup;
import io.baratine.core.OnLookup;
import io.baratine.core.Service;
import io.baratine.store.Store;

@Journal
@Service("/_counter")
public class CounterManagerImpl implements CounterManager
{
  @Inject @Lookup("store:///_counter")
  private Store<Long> _store;

  @OnLookup
  public CounterServiceImpl onLookup(String url)
  {
    int i = url.indexOf('/', 0);
    String id = url.substring(i + 1);

    String storeKey = "/" + id + "/counter";

    CounterServiceImpl counter = new CounterServiceImpl(id, storeKey, _store);

    return counter;
  }
}
