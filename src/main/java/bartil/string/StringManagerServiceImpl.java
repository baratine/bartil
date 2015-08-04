package bartil.string;

import java.util.LinkedHashMap;

import javax.inject.Inject;

import io.baratine.core.Journal;
import io.baratine.core.Lookup;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.ResultStream;
import io.baratine.core.Service;
import io.baratine.core.ServiceManager;
import io.baratine.core.ServiceRef;
import io.baratine.store.Store;

@Journal
@Service("/_string")
public class StringManagerServiceImpl implements StringManagerService
{
  @Inject @Lookup("store:///_string")
  private Store<String> _store;

  private transient long _watchCount;
  private transient LinkedHashMap<Long,WatchEntry> _watchMap;

  @OnLookup
  public StringServiceImpl onLookup(String url)
  {
    String storeKey = "/" + url + "/string";

    StringServiceImpl counter = new StringServiceImpl(url, storeKey, _store);

    return counter;
  }

  public void watch(String key, ResultStream<String> watcher,
                    Result<Long> result)
  {
    long watchId = _watchCount++;

    ServiceManager manager = ServiceManager.getCurrent();

    if (! key.startsWith("/")) {
      key = "/" + key;
    }

    StringService service = manager.lookup("/string-internal" + key).as(StringService.class);

    WatchEntry entry = new WatchEntry(watchId, service);

    entry.watch(watcher, result);
  }

  class WatchEntry {
    private long _watchId;
    private long _childWatchId;

    private StringService _service;

    public WatchEntry(long watchId, StringService service)
    {
      _watchId = watchId;
      _service = service;
    }

    public void watch(ResultStream<String> watcher, Result<Long> result)
    {
      _service.watch(watcher, childWatchId -> {
        _childWatchId = childWatchId;

        _watchMap.put(_watchId, this);

        result.complete(_watchId);
      });
    }

    public void unwatch(Result<Boolean> result)
    {
      _service.unwatch(_childWatchId, isSuccessful -> {
        WatchEntry entry = _watchMap.remove(_watchId);

        if (entry != null) {
          result.complete(isSuccessful);
        }
        else {
          result.complete(false);
        }
      });
    }
  }
}
