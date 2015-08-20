package bartil.list;

import io.baratine.core.Lookup;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;

import java.util.List;

import javax.inject.Inject;

@Service("public:///list")
public class ListManagerFacade<T>
{
  @Inject @Lookup("/_list")
  private ServiceRef _serviceRef;

  @OnLookup
  public ListServiceFacade<T> onLookup(String url)
  {
    ListService<T> list = _serviceRef.lookup(url).as(ListService.class);

    return new ListServiceFacade<>(list);
  }

  static class ListServiceFacade<T> implements ListService<T> {
    private ListService<T> _list;

    public ListServiceFacade(ListService<T> list)
    {
      _list = list;
    }

    public void get(int index, Result<T> result)
    {
      _list.get(index, result);
    }

    public void getRange(int start, int end, Result<List<T>> result)
    {
      _list.getRange(start, end, result);
    }

    public void getAll(Result<List<T>> result)
    {
      _list.getAll(result);
    }

    public void pushHead(T value, Result<Integer> result)
    {
      _list.pushHead(value, result);
    }

    public void pushTail(T value, Result<Integer> result)
    {
      _list.pushTail(value, result);
    }

    public void pushHeadMultiple(Result<Integer> result, T ... values)
    {
      _list.pushHeadMultiple(result, values);
    }

    public void pushTailMultiple(Result<Integer> result, T ... values)
    {
      _list.pushTailMultiple(result, values);
    }

    public void popHead(Result<T> result)
    {
      _list.popHead(result);
    }

    public void popTail(Result<T> result)
    {
      _list.popTail(result);
    }

    public void set(int index, T value, Result<Boolean> result)
    {
      _list.set(index, value, result);
    }

    public void remove(int index, Result<Integer> result)
    {
      _list.remove(index, result);
    }

    public void removeValue(T value, int count, Result<Integer> result)
    {
      _list.removeValue(value, count, result);
    }

    public void trim(int start, int end, Result<Integer> result)
    {
      _list.trim(start, end, result);
    }

    public void clear(Result<Integer> result)
    {
      _list.clear(result);
    }

    public void size(Result<Integer> result)
    {
      _list.size(result);
    }

    public void delete(Result<Boolean> result)
    {
      _list.delete(result);
    }

    public void exists(Result<Boolean> result)
    {
      _list.exists(result);
    }
  }
}
