package bartil.list;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.baratine.core.Lookup;
import io.baratine.core.ServiceExceptionExecution;
import io.baratine.core.ServiceRef;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import bartil.list.ListManagerImpl;
import bartil.list.ListServiceSync;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;

@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(services = {ListManagerImpl.class}, pod = "mypod",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "WARNING"),
          @ConfigurationBaratine.Log(name = "examples.cache.list", level = "FINER")})
public class ListServiceTest
{
  @Inject @Lookup("pod://mypod/list")
  private ServiceRef _listManagerRef;

  @Inject
  private RunnerBaratine _testContext;

  private static AtomicInteger _count = new AtomicInteger();

  @Test
  public void testPushHead()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(0, service.size());

    Assert.assertEquals(1, service.pushHead("aaa"));
    Assert.assertEquals(2, service.pushHead("bbb"));

    restartBaratine();

    Assert.assertEquals("bbb", service.get(0));
    Assert.assertEquals("aaa", service.get(1));
  }

  @Test
  public void testPushTail()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(1, service.pushTail("aaa"));
    Assert.assertEquals(2, service.pushTail("bbb"));

    restartBaratine();

    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("bbb", service.get(1));
  }

  @Test
  public void testPushHeadMultiple()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(3, service.pushHeadMultiple("aaa", "bbb", "ccc"));

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("ccc", service.get(0));
    Assert.assertEquals("bbb", service.get(1));
    Assert.assertEquals("aaa", service.get(2));
  }

  @Test
  public void testPushTailMultiple()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(3, service.pushTailMultiple("aaa", "bbb", "ccc"));

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("bbb", service.get(1));
    Assert.assertEquals("ccc", service.get(2));
  }

  @Test
  public void testPopHead()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(3, service.pushTailMultiple("aaa", "bbb", "ccc"));

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("aaa", service.popHead());

    restartBaratine();

    Assert.assertEquals(2, service.size());
    Assert.assertEquals("bbb", service.popHead());

    Assert.assertEquals(1, service.size());
    Assert.assertEquals("ccc", service.popHead());

    Assert.assertEquals(0, service.size());
    Assert.assertEquals(null, service.popHead());
  }

  @Test
  public void testPopTail()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(3, service.pushTailMultiple("aaa", "bbb", "ccc"));

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("ccc", service.popTail());

    restartBaratine();

    Assert.assertEquals(2, service.size());
    Assert.assertEquals("bbb", service.popTail());

    Assert.assertEquals(1, service.size());
    Assert.assertEquals("aaa", service.popTail());

    Assert.assertEquals(0, service.size());
    Assert.assertEquals(null, service.popTail());
  }

  @Test
  public void testSet()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(3, service.pushTailMultiple("aaa", "bbb", "ccc"));

    Assert.assertEquals(true, service.set(1, "ddd"));
    Assert.assertEquals(3, service.size());
    Assert.assertEquals("ddd", service.get(1));

    restartBaratine();

    Assert.assertEquals(false, service.set(5, "eee"));
    Assert.assertEquals(false, service.set(-1, "fff"));

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("ddd", service.get(1));
    Assert.assertEquals("ccc", service.get(2));
  }

  @Test
  public void testRemove()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(3, service.pushTailMultiple("aaa", "bbb", "ccc"));

    Assert.assertEquals(1, service.remove(1));

    Assert.assertEquals(2, service.size());
    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("ccc", service.get(1));

    Assert.assertEquals(0, service.remove(5));
    Assert.assertEquals(0, service.remove(-1));

    restartBaratine();

    Assert.assertEquals(2, service.size());
    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("ccc", service.get(1));
  }

  @Test
  public void testRemoveValue()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(3, service.pushTailMultiple("aaa", "bbb", "ccc"));

    Assert.assertEquals(1, service.removeValue("bbb", 5));

    Assert.assertEquals(2, service.size());
    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("ccc", service.get(1));

    Assert.assertEquals(0, service.removeValue("foo", 5));

    restartBaratine();

    Assert.assertEquals(2, service.size());
    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("ccc", service.get(1));
  }

  @Test
  public void testRemoveValueMultiple()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(5, service.pushTailMultiple("aaa", "bbb", "ccc", "bbb", "bbb"));

    Assert.assertEquals(3, service.removeValue("bbb", 5));

    Assert.assertEquals(2, service.size());
    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("ccc", service.get(1));
  }

  @Test
  public void testRemoveValueMultipleSubset()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(5, service.pushTailMultiple("aaa", "bbb", "ccc", "bbb", "bbb"));

    Assert.assertEquals(2, service.removeValue("bbb", 2));

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("ccc", service.get(1));
    Assert.assertEquals("bbb", service.get(2));
  }

  @Test
  public void testRemoveValueMultipleDescending()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(5, service.pushTailMultiple("aaa", "bbb", "ccc", "bbb", "bbb"));

    Assert.assertEquals(2, service.removeValue("bbb", -2));

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("bbb", service.get(1));
    Assert.assertEquals("ccc", service.get(2));
  }

  @Test
  public void testTrim()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");

    Assert.assertEquals(2, service.trim(0, 2));

    Assert.assertEquals(2, service.size());

    restartBaratine();

    Assert.assertEquals(2, service.size());
    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("bbb", service.get(1));
  }

  @Test
  public void testTrimMiddle()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");

    Assert.assertEquals(3, service.trim(1, 4));

    Assert.assertEquals(3, service.size());

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("bbb", service.get(0));
    Assert.assertEquals("ccc", service.get(1));
    Assert.assertEquals("ddd", service.get(2));
  }

  @Test
  public void testTrimEnd()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");

    Assert.assertEquals(3, service.trim(3, 9));
    Assert.assertEquals(3, service.size());

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("ddd", service.get(0));
    Assert.assertEquals("eee", service.get(1));
    Assert.assertEquals("fff", service.get(2));
  }

  @Test
  public void testTrimNegativeStartEnd()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");

    Assert.assertEquals(3, service.trim(-5, -2));
    Assert.assertEquals(3, service.size());

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("bbb", service.get(0));
    Assert.assertEquals("ccc", service.get(1));
    Assert.assertEquals("ddd", service.get(2));
  }

  @Test
  public void testTrimNegativeStart()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");

    Assert.assertEquals(3, service.trim(-5, 4));
    Assert.assertEquals(3, service.size());

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("bbb", service.get(0));
    Assert.assertEquals("ccc", service.get(1));
    Assert.assertEquals("ddd", service.get(2));
  }

  @Test
  public void testTrimNegativeEnd()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");

    Assert.assertEquals(3, service.trim(1, -2));
    Assert.assertEquals(3, service.size());

    restartBaratine();

    Assert.assertEquals(3, service.size());
    Assert.assertEquals("bbb", service.get(0));
    Assert.assertEquals("ccc", service.get(1));
    Assert.assertEquals("ddd", service.get(2));
  }

  @Test
  public void testTrimStartLargerThanEnd()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");

    Assert.assertEquals(0, service.trim(4, 3));
    Assert.assertEquals(0, service.size());

    restartBaratine();

    Assert.assertEquals(0, service.size());
  }

  @Test
  public void testClear()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");

    Assert.assertEquals(6, service.clear());
    Assert.assertEquals(0, service.size());

    restartBaratine();

    Assert.assertEquals(0, service.size());
  }

  @Test
  public void testExists()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(false, service.exists());

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");
    Assert.assertEquals(true, service.exists());

    restartBaratine();

    Assert.assertEquals(true, service.exists());
  }

  @Test
  public void testDelete()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");
    Assert.assertEquals(true, service.exists());

    restartBaratine();

    Assert.assertEquals(true, service.exists());

    Assert.assertEquals(true, service.delete());
    Assert.assertEquals(false, service.exists());

    restartBaratine();

    Assert.assertEquals(false, service.exists());
  }

  //@Test
  public void testGetRange()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTail("aaa");
    service.pushTail("bbb");
    service.pushTail("ccc");
    service.pushTail("ddd");
    service.pushTail("eee");

    restartBaratine();

    List<String> list;

    list = service.getRange(0, 2);

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("aaa", list.get(0));
    Assert.assertEquals("bbb", list.get(1));

    list = service.getRange(2, 5);

    Assert.assertEquals(3, list.size());
    Assert.assertEquals("ccc", list.get(0));
    Assert.assertEquals("ddd", list.get(1));
    Assert.assertEquals("eee", list.get(1));
  }

  @Test
  public void testGetOutOfBounds()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    Assert.assertEquals(0, service.size());

    try {
      Assert.assertEquals(null, service.get(5));

      Assert.fail();
    }
    catch (ServiceExceptionExecution e) {
      Assert.assertTrue(e.getCause() instanceof NullPointerException);
    }

    Assert.assertEquals(1, service.pushTail("aaa"));
    Assert.assertEquals(2, service.pushTail("bbb"));

    restartBaratine();

    Assert.assertEquals("aaa", service.get(0));
    Assert.assertEquals("bbb", service.get(1));

    try {
      Assert.assertEquals("ccc", service.get(2));

      Assert.fail();
    }
    catch (ServiceExceptionExecution e) {
      Assert.assertTrue(e.getCause() instanceof IndexOutOfBoundsException);
    }
  }

  @Test
  public void testWatch()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);
  }

  @Test
  public void testLookup()
  {
    String id0 = "foo/bar:" + _count.getAndIncrement();
    ListServiceSync<String> service0 = lookup(id0);
    service0.pushHead("aaa");

    String id1 = "foo/bar:" + _count.getAndIncrement();
    ListServiceSync<String> service1 = lookup(id1);
    service1.pushHead("bbb");
    service1.pushHead("ccc");

    Assert.assertEquals(1, service0.size());
    Assert.assertEquals(2, service1.size());
  }

  //@Test
  /*
  public void testStream()
  {
    String id = "" + _count.getAndIncrement();
    ListServiceSync<String> service = lookup(id);

    service.pushTailMultiple("aaa", "bbb", "ccc", "ddd", "eee", "fff");

    Iterable<String> iterable = service.stream(2, 4).iter();
    Iterator<String> iter = iterable.iterator();

    Assert.assertEquals("ccc", iter.next());
    Assert.assertEquals("ddd", iter.next());

    Assert.assertEquals(false, iter.hasNext());
  }
  */

  private void restartBaratine()
  {
    //_testContext.closeImmediate();
    //_testContext.start();
  }

  private ListServiceSync<String> lookup(String id)
  {
    ListServiceSync<String> service
      = _listManagerRef.lookup("/" + id).as(ListServiceSync.class);

    return service;
  }
}
