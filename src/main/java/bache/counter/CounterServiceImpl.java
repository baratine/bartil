package example.cache.counter;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.store.Store;

public class CounterServiceImpl implements CounterService
{
  private String _id;
  private String _storeKey;
  private Store<Long> _store;

  private long _count;

  private boolean _isValid;

  public CounterServiceImpl(String id, String storeKey, Store<Long> store)
  {
    _id = id;
    _storeKey = storeKey;

    _store = store;
  }

  @Override
  public void get(Result<Long> result)
  {
    result.complete(_count);
  }

  @Modify
  @Override
  public void set(long value, Result<Boolean> result)
  {
    _count = value;

    _isValid = true;

    result.complete(true);
  }

  @Modify
  @Override
  public void increment(long value, Result<Long> result)
  {
    _count += value;

    _isValid = true;

    result.complete(_count);
  }

  @Modify
  @Override
  public void delete(Result<Boolean> result)
  {
    _count = 0;

    _isValid = false;

    result.complete(true);
  }

  @Override
  public void exists(Result<Boolean> result)
  {
    result.complete(_isValid);
  }

  @OnLoad
  public void onLoad(Result<Boolean> result)
  {
    _store.get(_storeKey, count -> {
      if (count != null) {
        _count = count.longValue();
      }
      else {
        _isValid = false;
      }

      result.complete(true);
    });
  }

  @OnSave
  public void onSave(Result<Boolean> result)
  {
    if (_isValid) {
      _store.put(_storeKey, _count);
    }
    else {
      _store.remove(_storeKey);
    }

    result.complete(true);
  }
}
