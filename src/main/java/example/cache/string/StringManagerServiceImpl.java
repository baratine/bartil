package example.cache.string;

import javax.inject.Inject;

import io.baratine.core.Journal;
import io.baratine.core.Lookup;
import io.baratine.core.OnLookup;
import io.baratine.core.Service;
import io.baratine.store.Store;

@Journal
@Service("public:///string")
public class StringManagerServiceImpl implements StringManagerService
{
  @Inject @Lookup("store:///string")
  private Store<String> _store;

  @OnLookup
  public StringServiceImpl onLookup(String url)
  {
    int i = url.indexOf('/', 0);
    String id = url.substring(i + 1);

    String storeKey = "/" + id + "/string";

    StringServiceImpl counter = new StringServiceImpl(id, storeKey, _store);

    return counter;
  }
}
