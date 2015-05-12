package example.cache.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;

import io.baratine.core.Lookup;
import io.baratine.core.ServiceRef;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(services = {MapManagerServiceImpl.class}, pod = "mypod",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "WARNING"),
          @ConfigurationBaratine.Log(name = "examples.cache.map", level = "FINER")})
public class MapServiceTest
{
  @Inject @Lookup("pod://mypod/map")
  private ServiceRef _mapManagerRef;

  @Inject
  private RunnerBaratine _testContext;

  private static AtomicInteger _count = new AtomicInteger();

  @Test
  public void testGetNew()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(-1, service.size());
    Assert.assertEquals(null, service.get("foo"));
    Assert.assertEquals(-1, service.size());

    restartBaratine();

    Assert.assertEquals(null, service.get("foo"));
    Assert.assertEquals(-1, service.size());
  }

  @Test
  public void testGetMultiple()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    List<String> list = service.getMultiple("bbb", "ccc", "ddd");

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("222", list.get(0));
    Assert.assertEquals("333", list.get(1));
  }

  @Test
  public void testGetKeys()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    List<String> list = service.getKeys();

    Collections.sort(list);

    Assert.assertEquals(3, list.size());
    Assert.assertEquals("aaa", list.get(0));
    Assert.assertEquals("bbb", list.get(1));
    Assert.assertEquals("ccc", list.get(2));
  }

  //@Test
  public void testGetAll()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    Map<String,String> map = service.getAll();

    Assert.assertEquals(3, map.size());
    Assert.assertEquals("111", map.get("aaa"));
    Assert.assertEquals("222", map.get("bbb"));
    Assert.assertEquals("333", map.get("ccc"));
  }

  @Test
  public void testPut()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(1, service.put("aaa", "111"));
    Assert.assertEquals(2, service.put("bbb", "222"));
    Assert.assertEquals(3, service.put("ccc", "333"));
    Assert.assertEquals(3, service.size());

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals("222", service.get("bbb"));
    Assert.assertEquals("333", service.get("ccc"));
  }

  @Test
  public void testPutOverwrite()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(1, service.put("aaa", "111"));
    Assert.assertEquals(2, service.put("bbb", "222"));
    Assert.assertEquals(3, service.put("ccc", "333"));

    Assert.assertEquals(3, service.put("ccc", "444"));
    Assert.assertEquals(3, service.size());

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals("222", service.get("bbb"));
    Assert.assertEquals("444", service.get("ccc"));
  }

  @Test
  public void testPutIfAbsent()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(true, service.putIfAbsent("aaa", "111"));
    Assert.assertEquals(true, service.putIfAbsent("bbb", "222"));
    Assert.assertEquals(true, service.putIfAbsent("ccc", "333"));

    Assert.assertEquals(false, service.putIfAbsent("ccc", "444"));

    Assert.assertEquals(3, service.size());

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals("222", service.get("bbb"));
    Assert.assertEquals("333", service.get("ccc"));
  }

  //@Test
  public void testPutMap()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    HashMap<String,String> map = new HashMap<>();

    map.put("aaa", "111");
    map.put("bbb", "222");
    map.put("ccc", "333");

    Assert.assertEquals(3, service.putMap(map));
    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals("222", service.get("bbb"));
    Assert.assertEquals("333", service.get("ccc"));

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals("222", service.get("bbb"));
    Assert.assertEquals("333", service.get("ccc"));
  }

  //@Test
  public void testPutMapOverwrite()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    HashMap<String,String> map = new HashMap<>();

    service.put("aaa", "111");
    service.put("bbb", "222");

    map.put("bbb", "555");
    map.put("ccc", "333");
    map.put("ddd", "444");

    Assert.assertEquals(4, service.putMap(map));

    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals("555", service.get("bbb"));
    Assert.assertEquals("333", service.get("ccc"));
    Assert.assertEquals("444", service.get("ddd"));

    restartBaratine();

    Assert.assertEquals(4, service.size());
    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals("555", service.get("bbb"));
    Assert.assertEquals("333", service.get("ccc"));
    Assert.assertEquals("444", service.get("ddd"));
  }

  @Test
  public void testContainsKey()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(false, service.containsKey("aaa"));

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    Assert.assertEquals(true, service.containsKey("bbb"));
    Assert.assertEquals(false, service.containsKey("foo"));
  }

  @Test
  public void testContainsValue()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(false, service.containsValue("111"));

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    Assert.assertEquals(true, service.containsValue("222"));
    Assert.assertEquals(false, service.containsValue("aaa"));
  }

  @Test
  public void testRemove()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(0, service.remove("aaa"));

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    Assert.assertEquals(1, service.remove("bbb"));

    Assert.assertEquals(2, service.size());
    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals("333", service.get("ccc"));

    restartBaratine();

    Assert.assertEquals(2, service.size());
    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals("333", service.get("ccc"));
  }

  @Test
  public void testRemoveMultiple()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(0, service.remove("aaa"));

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    Assert.assertEquals(2, service.removeMultiple("bbb", "ccc", "ddd", "eee"));

    Assert.assertEquals(1, service.size());
    Assert.assertEquals("111", service.get("aaa"));

    restartBaratine();

    Assert.assertEquals(1, service.size());
    Assert.assertEquals("111", service.get("aaa"));
  }

  @Test
  public void testRename()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(false, service.rename("bbb", "foo"));

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    Assert.assertEquals(true, service.rename("bbb", "ddd"));
    Assert.assertEquals(false, service.rename("foo", "bar"));

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals(null, service.get("bbb"));
    Assert.assertEquals("333", service.get("ccc"));
    Assert.assertEquals("222", service.get("ddd"));

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("111", service.get("aaa"));
    Assert.assertEquals(null, service.get("bbb"));
    Assert.assertEquals("333", service.get("ccc"));
    Assert.assertEquals("222", service.get("ddd"));
  }

  @Test
  public void testClear()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(0, service.clear());

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    Assert.assertEquals(3, service.clear());
    Assert.assertEquals(0, service.size());
    Assert.assertEquals(0, service.clear());
    Assert.assertEquals(true, service.exists());

    restartBaratine();

    Assert.assertEquals(0, service.size());
    Assert.assertEquals(true, service.exists());
  }

  @Test
  public void testExists()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(false, service.exists());

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    Assert.assertEquals(true, service.exists());

    restartBaratine();

    Assert.assertEquals(true, service.exists());
  }

  @Test
  public void testDelete()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(false, service.delete());

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    Assert.assertEquals(true, service.delete());

    restartBaratine();

    Assert.assertEquals(false, service.exists());
  }

  @Test
  public void testDeleteAfterSave()
  {
    String id = "" + _count.getAndIncrement();

    MapServiceSync<String,String> service = lookup(id);

    Assert.assertEquals(false, service.delete());

    service.put("aaa", "111");
    service.put("bbb", "222");
    service.put("ccc", "333");

    restartBaratine();

    Assert.assertEquals(true, service.exists());
    Assert.assertEquals(true, service.delete());
    Assert.assertEquals(false, service.exists());

    restartBaratine();
    Assert.assertEquals(false, service.exists());
  }

  private MapServiceSync<String,String> lookup(String id)
  {
    MapServiceSync<String,String> service
      = _mapManagerRef.lookup("/" + id).as(MapServiceSync.class);

    return service;
  }

  private void restartBaratine()
  {
    //_testContext.closeImmediate();
    //_testContext.start();
  }
}
