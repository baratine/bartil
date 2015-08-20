package bartil.tree;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.baratine.core.Lookup;
import io.baratine.core.OnLookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;

@Service("public:///tree")
public class TreeManagerFacade<K,V>
{
  @Inject @Lookup("/_tree")
  private ServiceRef _serviceRef;

  @OnLookup
  public TreeServiceFacade<K,V> onLookup(String url)
  {
    TreeService<K,V> tree = _serviceRef.lookup(url).as(TreeService.class);

    return new TreeServiceFacade<>(tree);
  }

  static class TreeServiceFacade<K,V> implements TreeService<K,V> {
    private TreeService<K,V> _tree;

    public TreeServiceFacade(TreeService<K,V> tree)
    {
      _tree = tree;
    }

    public void get(K key, Result<V> result)
    {
      _tree.get(key, result);
    }

    public void put(K key, V score, Result<Integer> result)
    {
      _tree.put(key, score, result);
    }

    public void getRange(int start, int end, Result<List<Map<K,V>>> result)
    {
      _tree.getRange(start, end, result);
    }

    public void getRangeDescending(int start, int end, Result<List<Map<K,V>>> result)
    {
      _tree.getRangeDescending(start, end, result);
    }

    public void getRangeKeys(int start, int end, Result<List<K>> result)
    {
      _tree.getRangeKeys(start, end, result);
    }

    public void getRangeDescendingKeys(int start, int end, Result<List<K>> result)
    {
      _tree.getRangeDescendingKeys(start, end, result);
    }

    public void size(Result<Integer> result)
    {
      _tree.size(result);
    }

    public void clear(Result<Integer> result)
    {
      _tree.clear(result);
    }

    public void remove(K key, Result<Integer> result)
    {
      _tree.remove(key, result);
    }

    public void delete(Result<Boolean> result)
    {
      _tree.delete(result);
    }

    public void exists(Result<Boolean> result)
    {
      _tree.exists(result);
    }
  }
}
