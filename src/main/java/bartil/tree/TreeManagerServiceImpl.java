package bartil.tree;

import io.baratine.core.Journal;
import io.baratine.core.OnLookup;
import io.baratine.core.Lookup;
import io.baratine.core.Service;
import io.baratine.store.Store;

import java.util.TreeMap;

import javax.inject.Inject;

@Journal
@Service("/_tree")
public class TreeManagerServiceImpl<K,V> implements TreeManagerService<K,V>
{
  @Inject @Lookup("store:///_tree")
  private Store<TreeMap<K,V>> _store;

  @OnLookup
  public TreeServiceImpl<K,V> onLookup(String url)
  {
    String storeKey = "/" + url + "/tree";

    TreeServiceImpl<K,V> score = new TreeServiceImpl<K,V>(url, storeKey, _store);

    return score;
  }
}
