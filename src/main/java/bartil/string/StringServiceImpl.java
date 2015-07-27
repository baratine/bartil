package bartil.string;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.core.ResultStream;
import io.baratine.store.Store;

public class StringServiceImpl implements StringService
{
  private String _id;
  private String _storeKey;
  private Store<String> _store;

  private String _value;

  private transient long _watchCount;
  private transient LinkedHashMap<Long,ResultStream<String>> _watchMap;

  public StringServiceImpl(String id, String storeKey, Store<String> store)
  {
    _id = id;
    _storeKey = storeKey;

    _store = store;
  }

  @Override
  public void get(Result<String> result)
  {
    result.complete(_value);
  }

  @Modify
  @Override
  public void set(String value, Result<Boolean> result)
  {
    _value = value;

    notifyWatchers();

    result.complete(true);
  }

  @Modify
  @Override
  public void delete(Result<Boolean> result)
  {
    _value = null;

    notifyWatchers();

    _watchMap = null;

    result.complete(true);
  }

  @Override
  public void exists(Result<Boolean> result)
  {
    result.complete(_value != null);
  }

  @Override
  public void watch(ResultStream<String> watcher, Result<Long> result)
  {
    if (_watchMap == null) {
      _watchMap = new LinkedHashMap<>();
    }

    pruneWatchers();

    long id = _watchCount++;
    _watchMap.put(id, watcher);

    result.complete(id);
  }

  @Override
  public void unwatch(long id, Result<Boolean> result)
  {
    boolean isSuccessful = false;

    if (_watchMap != null) {
      ResultStream<String> watcher = _watchMap.remove(id);

      if (watcher != null) {
        isSuccessful = true;
      }
    }

    result.complete(isSuccessful);
  }

  private void notifyWatchers()
  {
    LinkedList<Long> cancelledList = null;

    if (_watchMap == null) {
      return;
    }

    for (Map.Entry<Long,ResultStream<String>> entry : _watchMap.entrySet()) {
      ResultStream<String> watcher = entry.getValue();

      if (watcher.isCancelled()) {
        if (cancelledList == null) {
          cancelledList = new LinkedList<>();
        }

        cancelledList.add(entry.getKey());
      }
      else {
        watcher.accept(_value);
      }
    }

    if (cancelledList != null) {
      for (Long id : cancelledList) {
        _watchMap.remove(id);
      }
    }
  }

  private void pruneWatchers()
  {
    LinkedList<Long> cancelledList = null;

    for (Map.Entry<Long,ResultStream<String>> entry : _watchMap.entrySet()) {
      ResultStream<String> watcher = entry.getValue();

      if (watcher.isCancelled()) {
        if (cancelledList == null) {
          cancelledList = new LinkedList<>();
        }

        cancelledList.add(entry.getKey());
      }
    }

    if (cancelledList != null) {
      for (Long id : cancelledList) {
        _watchMap.remove(id);
      }
    }
  }

  @OnLoad
  public void onLoad(Result<Boolean> result)
  {
    _store.get(_storeKey, value -> {
      _value = value;

      result.complete(true);
    });
  }

  @OnSave
  public void onSave(Result<Boolean> result)
  {
    if (_value != null) {
      _store.put(_storeKey, _value);
    }
    else {
      _store.remove(_storeKey);
    }

    result.complete(true);
  }
}
