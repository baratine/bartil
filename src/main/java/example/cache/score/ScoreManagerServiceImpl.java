package example.cache.score;

import io.baratine.core.Journal;
import io.baratine.core.OnLookup;
import io.baratine.core.Lookup;
import io.baratine.core.Result;
import io.baratine.core.Service;
import io.baratine.core.ServiceRef;
import io.baratine.core.Services;
import io.baratine.store.Store;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import javax.inject.Inject;

@Journal
@Service("public:///score")
public class ScoreManagerServiceImpl implements ScoreSetManagerService
{
  @Inject @Lookup("store:///score")
  private Store<TreeMap<String,Long>> _store;

  @OnLookup
  public ScoreServiceImpl onLookup(String url)
  {
    int i = url.lastIndexOf('/');
    String id = url.substring(i + 1);

    String storeKey = "/" + id + "/score";

    ScoreServiceImpl score = new ScoreServiceImpl(id, storeKey, _store);

    return score;
  }
}
