package bache.string;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.store.Store;

public class StringServiceImpl implements StringService
{
  private String _id;
  private String _storeKey;
  private Store<String> _store;

  private String _value;

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

    result.complete(true);
  }

  @Modify
  @Override
  public void delete(Result<Boolean> result)
  {
    _value = null;

    result.complete(true);
  }

  @Override
  public void exists(Result<Boolean> result)
  {
    result.complete(_value != null);
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
