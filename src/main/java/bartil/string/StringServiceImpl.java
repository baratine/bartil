package bartil.string;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.core.ResultStream;
import io.baratine.pubsub.PubSubService;
import io.baratine.store.Store;

public class StringServiceImpl implements StringService
{
  private String _url;
  private Store<String> _store;

  private String _value;

  private transient PubSubService<String> _pubsub;

  public StringServiceImpl(String url, Store<String> store)
  {
    _url = url;

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

    result.complete(true);
  }

  @Override
  public void exists(Result<Boolean> result)
  {
    result.complete(_value != null);
  }

  private void notifyWatchers()
  {

  }

  @OnLoad
  public void onLoad(Result<Boolean> result)
  {
    _store.get(_url, value -> {
      _value = value;

      result.complete(true);
    });
  }

  @OnSave
  public void onSave(Result<Boolean> result)
  {
    if (_value != null) {
      _store.put(_url, _value);
    }
    else {
      _store.remove(_url);
    }

    result.complete(true);
  }
}
