package bartil.string;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.baratine.core.Lookup;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.ResultStream;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;
import io.baratine.stream.ResultStreamBuilder;

@Service("public:///string")
public class StringManagerFacade implements StringManager
{
  @Inject @Lookup("pod://pod/_string")
  private ServiceRef _serviceRef;

  @Inject @Lookup("pod://pod/_string")
  private StringManager _manager;

  @OnLookup
  public StringService onLookup(String url)
  {
    StringService service = _serviceRef.lookup(url).as(StringService.class);

    return new StringServiceFacade(service);
  }

  public void findValues(String regexp, Result<List<String>> result)
  {
    findValuesStream(regexp, new ResultStream<String>() {
        private ArrayList<String> _list = new ArrayList<>();

        public void accept(String key)
        {
          _list.add(key);
        }

        public void complete()
        {
          result.complete(_list);
        }

        public void fail(Throwable t)
        {
          t.printStackTrace();

          result.fail(t);
        }
    });
  }

  @Override
  public ResultStreamBuilder<String> findValuesStream(String regexp)
  {
    return _manager.findValuesStream(regexp);
  }

  @Override
  public void findValuesStream(String regexp, ResultStream<String> stream)
  {
    _manager.findValuesStream(regexp, stream);
  }

  static class StringServiceFacade implements StringService {
    private StringService _service;

    public StringServiceFacade(StringService service)
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
  }
}
