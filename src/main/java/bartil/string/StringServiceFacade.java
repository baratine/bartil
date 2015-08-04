package bartil.string;

import javax.inject.Inject;

import io.baratine.core.Lookup;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.ResultStream;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;

@Service("public:///string")
public class StringServiceFacade
{
  @Inject @Lookup("/_string")
  private ServiceRef _serviceRef;

  @OnLookup
  public StringServiceFacadeChild onLookup(String url)
  {
    StringService service = _serviceRef.lookup(url).as(StringService.class);

    return new StringServiceFacadeChild(service);
  }

  static class StringServiceFacadeChild implements StringService {
    private StringService _service;

    public StringServiceFacadeChild(StringService service)
    {
      _service = service;
    }

    public void get(Result<String> result)
    {
      _service.get(result);
    }

    public void set(String value, Result<Boolean> result)
    {
      _service.set(value, result);
    }

    public void delete(Result<Boolean> result)
    {
      _service.delete(result);
    }

    public void exists(Result<Boolean> result)
    {
      _service.exists(result);
    }

    public void watch(ResultStream<String> watcher, Result<Long> result)
    {
      _service.watch(watcher, result);
    }

    public void unwatch(long id, Result<Boolean> result)
    {
      _service.unwatch(id, result);
    }
  }
}
