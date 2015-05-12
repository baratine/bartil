package example.cache.string;

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
