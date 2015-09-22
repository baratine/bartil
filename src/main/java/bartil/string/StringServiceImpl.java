package bartil.string;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.pubsub.PubSubService;
import io.baratine.store.Store;

public class StringServiceImpl implements StringService
{
  private String _url;
  private Store<String> _store;
  private PubSubService<String> _pubsub;

  private String _value;

  public StringServiceImpl(String url,
                           Store<String> store,
                           PubSubService<String> pubsub)
  {
    _url = url;

    _store = store;

    _pubsub = pubsub;
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
    if (_pubsub != null) {
      _pubsub.publish(_value);
    }
  }

  @OnLoad
  public void onLoad(Result<Boolean> result)
  {
    System.out.println(getClass().getSimpleName() + ".onLoad0");

    _store.get(_url, result.from(value -> onLoadComplete(value)));
  }

  private boolean onLoadComplete(String value)
  {
    System.out.println(getClass().getSimpleName() + ".onLoad1: " + _url + " . " + value);

    _value = value;

    return true;
  }

  @OnSave
  public void onSave(Result<Boolean> result)
  {
    System.out.println(getClass().getSimpleName() + ".onSave0: " + _url + " . " + _value);

    if (_value != null) {
      _store.put(_url, _value);
    }
    else {
      _store.remove(_url);
    }

    result.complete(true);
  }
}
