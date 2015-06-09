package bache.string;

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
    String storeKey = "/" + url + "/string";

    StringServiceImpl counter = new StringServiceImpl(url, storeKey, _store);

    return counter;
  }
}
