package bache.string;

import java.util.concurrent.atomic.AtomicInteger;

import bache.string.StringManagerServiceImpl;

import com.caucho.junit.ConfigurationBaratine;
import com.caucho.junit.RunnerBaratine;

import io.baratine.core.Lookup;
import io.baratine.core.ServiceRef;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(RunnerBaratine.class)
@ConfigurationBaratine(services = {StringManagerServiceImpl.class}, pod = "mypod",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "WARNING"),
          @ConfigurationBaratine.Log(name = "examples.cache.string", level = "FINER")})
public class StringServiceTest
{
  @Inject @Lookup("pod://mypod/string")
  private ServiceRef _stringManagerRef;

  @Inject
  private RunnerBaratine _testContext;

  private static AtomicInteger _count = new AtomicInteger();

  @Test
  public void testNew()
  {
    String id = "" + _count.getAndIncrement();

    StringServiceSync service = lookup(id);

    Assert.assertEquals(null, service.get());
    Assert.assertEquals(true, service.set("aaa"));
    Assert.assertEquals("aaa", service.get());

    restartBaratine();

    Assert.assertEquals("aaa", service.get());
  }

  @Test
  public void testExists()
  {
    String id = "" + _count.getAndIncrement();

    StringServiceSync service = lookup(id);

    Assert.assertEquals(false, service.exists());
    Assert.assertEquals(true, service.set("aaa"));
    Assert.assertEquals(true, service.exists());

    restartBaratine();

    Assert.assertEquals("aaa", service.get());
  }

  @Test
  public void testDelete()
  {
    String id = "" + _count.getAndIncrement();

    StringServiceSync service = lookup(id);

    Assert.assertEquals(true, service.set("aaa"));
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
    StringServiceSync service0 = lookup(id0);
    service0.set("aaa");
    
    String id1 = "foo/bar:" + _count.getAndIncrement();
    StringServiceSync service1 = lookup(id1);
    service1.set("bbb");

    Assert.assertEquals("aaa", service0.get());
    Assert.assertEquals("bbb", service1.get());
  }

  private StringServiceSync lookup(String id)
  {
    StringServiceSync service
      = _stringManagerRef.lookup("/" + id).as(StringServiceSync.class);

    return service;
  }

  private void restartBaratine()
  {
    //_testContext.closeImmediate();
    //_testContext.start();
  }
}
