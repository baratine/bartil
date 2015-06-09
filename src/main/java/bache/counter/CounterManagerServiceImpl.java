package bache.counter;

import javax.inject.Inject;

import io.baratine.core.Journal;
import io.baratine.core.Lookup;
import io.baratine.core.OnLookup;
import io.baratine.core.Service;
import io.baratine.store.Store;

@Journal
@Service("public:///counter")
public class CounterManagerServiceImpl implements CounterManagerService
{
  @Inject @Lookup("store:///counter")
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
