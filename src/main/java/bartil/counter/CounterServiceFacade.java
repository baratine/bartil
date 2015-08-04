package bartil.counter;

import javax.inject.Inject;

import io.baratine.core.Lookup;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;

@Service("public:///counter")
public class CounterServiceFacade
{
  @Inject @Lookup("/_counter")
  private ServiceRef _serviceRef;

  @OnLookup
  public CounterServiceFacadeChild onLookup(String url)
  {
    CounterService counter = _serviceRef.lookup(url).as(CounterService.class);

    return new CounterServiceFacadeChild(counter);
  }

  static class CounterServiceFacadeChild implements CounterService {
    private CounterService _counter;

    public CounterServiceFacadeChild(CounterService counter)
    {
      _counter = counter;
    }

    public void get(Result<Long> result)
    {
      _counter.get(result);
    }

    public void set(long value, Result<Boolean> result)
    {
      _counter.set(value, result);
    }

    public void increment(long value, Result<Long> result)
    {
      _counter.increment(value, result);
    }

    public void delete(Result<Boolean> result)
    {
      _counter.delete(result);
    }

    public void exists(Result<Boolean> result)
    {
      _counter.exists(result);
    }
  }
}
