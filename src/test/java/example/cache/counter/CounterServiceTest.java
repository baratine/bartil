package example.cache.counter;

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
@ConfigurationBaratine(services = {CounterManagerServiceImpl.class}, pod = "mypod",
  logs = {@ConfigurationBaratine.Log(name = "com.caucho", level = "WARNING"),
          @ConfigurationBaratine.Log(name = "examples.cache.counter", level = "FINER")})
public class CounterServiceTest
{
  @Inject @Lookup("pod://mypod/counter")
  private ServiceRef _counterManagerRef;

  @Inject
  private RunnerBaratine _testContext;

  private static AtomicInteger _count = new AtomicInteger();

  @Test
  public void testNew()
  {
    String id = "" + _count.getAndIncrement();

    CounterServiceSync counter = lookup(id);

    Assert.assertEquals(0, counter.get());
    Assert.assertEquals(5, counter.increment(5));
    Assert.assertEquals(15, counter.increment(10));

    Assert.assertEquals(15, counter.get());

    restartBaratine();

    Assert.assertEquals(15, counter.get());
  }

  @Test
  public void testSet()
  {
    String id = "" + _count.getAndIncrement();

    CounterServiceSync counter = lookup(id);

    Assert.assertEquals(0, counter.get());
    Assert.assertEquals(true, counter.set(123));
    Assert.assertEquals(true, counter.set(456));

    Assert.assertEquals(456, counter.get());

    restartBaratine();

    Assert.assertEquals(456, counter.get());
  }

  @Test
  public void testMultipleCounters()
  {
    String id0 = "" + _count.getAndIncrement();
    String id1 = "" + _count.getAndIncrement();

    CounterServiceSync counter0 = lookup(id0);
    Assert.assertEquals(0, counter0.get());
    Assert.assertEquals(111, counter0.increment(111));

    CounterServiceSync counter1 = lookup(id1);
    Assert.assertEquals(555, counter1.increment(555));
    Assert.assertEquals(555, counter1.get());

    Assert.assertEquals(111, counter0.get());
  }

  @Test
  public void testMultipleLookups()
  {
    String id0 = "" + _count.getAndIncrement();

    CounterServiceSync counter0 = lookup(id0);
    Assert.assertEquals(0, counter0.get());
    Assert.assertEquals(111, counter0.increment(111));

    CounterServiceSync counter1 = lookup(id0);
    Assert.assertEquals(222, counter1.increment(111));

    Assert.assertEquals(222, counter0.get());
  }

  @Test
  public void testExists()
  {
    String id = "" + _count.getAndIncrement();

    CounterServiceSync counter = lookup(id);

    Assert.assertEquals(false, counter.exists());

    Assert.assertEquals(0, counter.get());
    Assert.assertEquals(false, counter.exists());

    Assert.assertEquals(5, counter.increment(5));
    Assert.assertEquals(true, counter.exists());

    restartBaratine();

    Assert.assertEquals(true, counter.exists());
    Assert.assertEquals(5, counter.get());
  }

  @Test
  public void testDelete()
  {
    String id = "" + _count.getAndIncrement();

    CounterServiceSync counter = lookup(id);

    Assert.assertEquals(5, counter.increment(5));
    Assert.assertEquals(true, counter.exists());

    Assert.assertEquals(true, counter.delete());
    Assert.assertEquals(false, counter.exists());

    restartBaratine();

    counter = lookup(id);
    Assert.assertEquals(false, counter.exists());
  }

  private CounterServiceSync lookup(String id)
  {
    CounterServiceSync service
      = _counterManagerRef.lookup("/" + id).as(CounterServiceSync.class);

    return service;
  }

  private void restartBaratine()
  {
    //_testContext.closeImmediate();
    //_testContext.start();
  }
}
