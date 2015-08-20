package bartil.string;

import java.util.regex.Pattern;

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
import io.baratine.stream.ResultStreamBuilder;

@Journal
@Service("/_string")
public class StringManagerImpl implements StringManager
{
  @Inject @Lookup("store:///_string")
  private Store<String> _store;

  @Inject @Lookup("/_string")
  private ServiceRef _selfRef;

  @OnLookup
  public StringServiceImpl onLookup(String url)
  {
    StringServiceImpl counter = new StringServiceImpl(url, _store);

    return counter;
  }

  @Override
  public ResultStreamBuilder<String> findValuesStream(String regexp)
  {
    throw new AbstractMethodError();
  }

  @Override
  public void findValuesStream(String regexp, ResultStream<String> stream)
  {
    _selfRef.save();

    _selfRef.as(this.getClass()).findValuesStreamImpl(regexp, stream);
  }

  public void findValuesStreamImpl(String regexp, ResultStream<String> stream)
  {
    Pattern pattern;

    if (regexp != null && regexp.length() > 0) {
      pattern = Pattern.compile(regexp);
    }
    else {
      pattern = null;
    }

    _store.find("true").forEach((key) -> {
        if (pattern == null || pattern.matcher(key).matches()) {
          stream.accept(key);
        }

    }).result(Void -> stream.complete());
  }
}
