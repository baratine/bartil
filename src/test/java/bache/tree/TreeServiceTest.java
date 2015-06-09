package bache.tree;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.baratine.core.Lookup;
import io.baratine.core.ServiceRef;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import bache.string.StringServiceSync;
import bache.tree.TreeManagerServiceImpl;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;

@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(services = {TreeManagerServiceImpl.class}, pod = "mypod",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "WARNING"),
          @ConfigurationBaratine.Log(name = "examples.cache.tree", level = "FINER")})
public class TreeServiceTest
{
  @Inject @Lookup("pod://mypod/tree")
  private ServiceRef _treeManagerRef;

  @Inject
  private RunnerBaratine _testContext;

  private static AtomicInteger _count = new AtomicInteger();

  @Test
  public void testNew()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    Assert.assertEquals(0, service.size());
    Assert.assertEquals(false, service.exists());
    Assert.assertEquals(null, service.get("aaa"));

    restartBaratine();

    Assert.assertEquals(0, service.size());
    Assert.assertEquals(false, service.exists());
    Assert.assertEquals(null, service.get("aaa"));
  }

  @Test
  public void testPut()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    Assert.assertEquals(1, service.put("aaa", 111));
    Assert.assertEquals(2, service.put("bbb", 333));
    Assert.assertEquals(3, service.put("ccc", 222));
    Assert.assertEquals(4, service.put("ddd", 555));
    Assert.assertEquals(5, service.put("eee", 444));

    Assert.assertEquals(5, service.size());
    Assert.assertEquals(Integer.valueOf(111), service.get("aaa"));
    Assert.assertEquals(Integer.valueOf(333), service.get("bbb"));
    Assert.assertEquals(Integer.valueOf(222), service.get("ccc"));
    Assert.assertEquals(Integer.valueOf(555), service.get("ddd"));
    Assert.assertEquals(Integer.valueOf(444), service.get("eee"));

    restartBaratine();

    Assert.assertEquals(5, service.size());
    Assert.assertEquals(Integer.valueOf(111), service.get("aaa"));
    Assert.assertEquals(Integer.valueOf(333), service.get("bbb"));
    Assert.assertEquals(Integer.valueOf(222), service.get("ccc"));
    Assert.assertEquals(Integer.valueOf(555), service.get("ddd"));
    Assert.assertEquals(Integer.valueOf(444), service.get("eee"));
  }

  //@Test
  public void testGetRangeKeys()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<String> list = service.getRangeKeys(0, 2);

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("aaa", list.get(0));
    Assert.assertEquals("ccc", list.get(1));
  }

  //@Test
  public void testGetRangeKeysMiddle()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<String> list = service.getRangeKeys(2, 4);

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("bbb", list.get(0));
    Assert.assertEquals("eee", list.get(1));
  }

  //@Test
  public void testGetRangeKeysEnd()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<String> list = service.getRangeKeys(3, 10);

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("eee", list.get(0));
    Assert.assertEquals("ddd", list.get(1));
  }

  //@Test
  public void testGetRangeKeysNegativeStartEnd()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<String> list = service.getRangeKeys(-4, -2);

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("ccc", list.get(0));
    Assert.assertEquals("bbb", list.get(1));
  }

  //@Test
  public void testGetRangeKeysNegativeStart()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<String> list = service.getRangeKeys(-4, 4);

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("ccc", list.get(0));
    Assert.assertEquals("bbb", list.get(1));
  }

  //@Test
  public void testGetRangeKeysNegativeEnd()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<String> list = service.getRangeKeys(2, -2);

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("ccc", list.get(0));
    Assert.assertEquals("bbb", list.get(1));
  }

  //@Test
  public void testGetRangeKeysStartLargerThanEnd()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<String> list = service.getRangeKeys(4, 2);

    Assert.assertEquals(0, list.size());
  }

  //@Test
  public void testGetRangeDescendingKeys()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<String> list = service.getRangeDescendingKeys(0, 2);

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("ddd", list.get(0));
    Assert.assertEquals("eee", list.get(1));
  }

  //@Test
  public void testGetRange()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<Map<String,Integer>> list = service.getRange(0, 2);

    Assert.assertEquals(2, list.size());

    Assert.assertEquals(1, list.get(0).size());
    Assert.assertEquals(Integer.valueOf(111), list.get(0).get("aaa"));

    Assert.assertEquals(1, list.get(1).size());
    Assert.assertEquals(Integer.valueOf(222), list.get(1).get("ccc"));
  }

  //@Test
  public void testGetRangeMiddle()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<Map<String,Integer>> list = service.getRange(2, 4);

    Assert.assertEquals(2, list.size());

    Assert.assertEquals(1, list.get(0).size());
    Assert.assertEquals(Integer.valueOf(333), list.get(0).get("bbb"));

    Assert.assertEquals(1, list.get(1).size());
    Assert.assertEquals(Integer.valueOf(444), list.get(1).get("eee"));
  }

  //@Test
  public void testGetRangeEnd()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<Map<String,Integer>> list = service.getRange(3, 10);

    Assert.assertEquals(2, list.size());

    Assert.assertEquals(1, list.get(0).size());
    Assert.assertEquals(Integer.valueOf(444), list.get(0).get("eee"));

    Assert.assertEquals(1, list.get(1).size());
    Assert.assertEquals(Integer.valueOf(555), list.get(1).get("ddd"));
  }

  //@Test
  public void testGetRangeNegativeStartEnd()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<Map<String,Integer>> list = service.getRange(-4, -2);

    Assert.assertEquals(2, list.size());

    Assert.assertEquals(1, list.get(0).size());
    Assert.assertEquals(Integer.valueOf(222), list.get(0).get("ccc"));

    Assert.assertEquals(1, list.get(1).size());
    Assert.assertEquals(Integer.valueOf(333), list.get(1).get("bbb"));
  }

  //@Test
  public void testGetRangeNegativeStart()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<Map<String,Integer>> list = service.getRange(-4, 4);

    Assert.assertEquals(2, list.size());

    Assert.assertEquals(1, list.get(0).size());
    Assert.assertEquals(Integer.valueOf(222), list.get(0).get("ccc"));

    Assert.assertEquals(1, list.get(1).size());
    Assert.assertEquals(Integer.valueOf(333), list.get(1).get("bbb"));
  }

  //@Test
  public void testGetRangeNegativeEnd()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<Map<String,Integer>> list = service.getRange(2, -2);

    Assert.assertEquals(2, list.size());

    Assert.assertEquals(1, list.get(0).size());
    Assert.assertEquals(Integer.valueOf(222), list.get(0).get("ccc"));

    Assert.assertEquals(1, list.get(1).size());
    Assert.assertEquals(Integer.valueOf(333), list.get(1).get("bbb"));
  }

  //@Test
  public void testGetRangeStartLargerThanEnd()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<Map<String,Integer>> list = service.getRange(4, 2);

    Assert.assertEquals(0, list.size());
  }

  //@Test
  public void testGetRangeDescending()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    List<Map<String,Integer>> list = service.getRange(0, 2);

    Assert.assertEquals(2, list.size());

    Assert.assertEquals(1, list.get(0).size());
    Assert.assertEquals(Integer.valueOf(555), list.get(0).get("ddd"));

    Assert.assertEquals(1, list.get(1).size());
    Assert.assertEquals(Integer.valueOf(444), list.get(1).get("eee"));
  }

  @Test
  public void testRemove()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    Assert.assertEquals(0, service.remove("aaa"));

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    Assert.assertEquals(1, service.remove("ccc"));

    Assert.assertEquals(4, service.size());
    Assert.assertEquals(Integer.valueOf(111), service.get("aaa"));
    Assert.assertEquals(Integer.valueOf(333), service.get("bbb"));
    Assert.assertEquals(null, service.get("ccc"));
    Assert.assertEquals(Integer.valueOf(555), service.get("ddd"));
    Assert.assertEquals(Integer.valueOf(444), service.get("eee"));

    restartBaratine();

    Assert.assertEquals(4, service.size());
    Assert.assertEquals(Integer.valueOf(111), service.get("aaa"));
    Assert.assertEquals(Integer.valueOf(333), service.get("bbb"));
    Assert.assertEquals(null, service.get("ccc"));
    Assert.assertEquals(Integer.valueOf(555), service.get("ddd"));
    Assert.assertEquals(Integer.valueOf(444), service.get("eee"));
  }

  @Test
  public void testClear()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    Assert.assertEquals(-1, service.clear());

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    Assert.assertEquals(5, service.size());
    Assert.assertEquals(5, service.clear());
    Assert.assertEquals(0, service.clear());

    Assert.assertEquals(0, service.size());
    Assert.assertEquals(true, service.exists());

    restartBaratine();

    Assert.assertEquals(0, service.size());
    Assert.assertEquals(true, service.exists());
  }

  @Test
  public void testExists()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    Assert.assertEquals(false, service.exists());

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    Assert.assertEquals(true, service.exists());

    restartBaratine();

    Assert.assertEquals(true, service.exists());
  }

  @Test
  public void testDelete()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    Assert.assertEquals(false, service.delete());

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    Assert.assertEquals(true, service.delete());

    restartBaratine();

    Assert.assertEquals(false, service.exists());
  }

  @Test
  public void testDeleteAfterSave()
  {
    String id = "" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service = lookup(id);

    service.put("aaa", 111);
    service.put("bbb", 333);
    service.put("ccc", 222);
    service.put("ddd", 555);
    service.put("eee", 444);

    restartBaratine();

    Assert.assertEquals(true, service.exists());
    Assert.assertEquals(true, service.delete());
    Assert.assertEquals(false, service.exists());

    restartBaratine();

    Assert.assertEquals(false, service.exists());
  }
  
  @Test
  public void testLookup()
  {
    String id0 = "foo/bar:" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service0 = lookup(id0);
    service0.put("aaa", 111);
    
    String id1 = "foo/bar:" + _count.getAndIncrement();
    TreeServiceSync<String,Integer> service1 = lookup(id1);
    service1.put("bbb", 222);
    service1.put("ccc", 333);

    Assert.assertEquals(1, service0.size());
    Assert.assertEquals(2, service1.size());
  }

  private TreeServiceSync<String,Integer> lookup(String id)
  {
    TreeServiceSync<String,Integer> service
      = _treeManagerRef.lookup("/" + id).as(TreeServiceSync.class);

    return service;
  }

  private void restartBaratine()
  {
    //_testContext.closeImmediate();
    //_testContext.start();
  }
}
