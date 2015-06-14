Bache is a Baratine service that exposes common data structures as a service.
Bache provides a map, list, tree, string, and counter type that are callable
from any client supporting WebSockets or HTTP.  You can store any object as
both the key or value field in the map, list, and tree data types.

In many ways, Bache is very similar to [Redis](http://redis.io/) and Bache
services should be familiar to Redis users.  Bache is used to show
that you can write any kind of Baratine service that runs about as fast as Redis, but
with much more functionality (and without having to resort to Lua scripting, as is
the case with Redis).

Usage
==========
Bache provides the following services:

* [/map](src/main/java/bache/map/MapService.java)
* [/list](src/main/java/bache/list/ListService.java)
* [/tree](src/main/java/bache/tree/TreeService.java)
* [/string](src/main/java/bache/string/StringService.java)
* [/counter](src/main/java/bache/counter/CounterService.java)

To call the /map service for example (assuming you deployed it to the default pod):

Java
------
    import io.baratine.core.ResultFuture;
    import com.caucho.amp.hamp.ClientHamp;
    import bache.map.MapService;
    import bache.map.MapServiceSync;

    ClientHamp client = new ClientHamp("http://localhost:8085/s/pod";
    
    MapService<String,String> map = client.lookup("/map/123").as(MapService.class);
    
    // calling it asynchronously    
    map.put("foo", "aaa", /* int */ size -> {
      System.out.println("new size is: " + size);
    });
    
    Thread.sleep(2000);
    
    // calling it synchronously
    ResultFuture<String> valueFuture = new ResultFuture<>();
    
    map.get("foo", valueFuture);
    System.out.println("value is: " + valueFuture.get());
    
    // or calling it synchronously using a synchronous Java interface
    MapServiceSync<String,String> mapSync = client.lookup("/map/123").as(MapServiceSync.class);
    System.out.println("value is: " + map.get("foo"));

When you do a lookup(), you may cast the proxy to whatever interface class you want.  The Baratine proxy will do all the magic marshalling arguments back and forth.  Bache provides both asynchronous (e.g. MapService) and synchronous
(e.g. MapServiceSync) interfaces for its services.

URL         | Async API     | Sync API
------------|---------------|---------
/map        | [bache.map.MapService](src/main/java/bache/map/MapService.java) | [bache.map.MapServiceSync](src/main/java/bache/map/MapServiceSync.java)
/list        | [bache.list.ListService](src/main/java/bache/list/ListService.java) | [bache.list.ListServiceSync](src/main/java/bache/list/ListServiceSync.java)
/tree        | [bache.tree.TreeService](src/main/java/bache/tree/TreeService.java) | [bache.tree.TreeServiceSync](src/main/java/bache/tree/TreeServiceSync.java)
/string        | [bache.string.StringService](src/main/java/bache/string/StringService.java) | [bache.string.StringServiceSync](src/main/java/bache/string/StringServiceSync.java)
/counter        | [bache.counter.CounterService](src/main/java/bache/counter/CounterService.java) | [bache.counter.CounterServiceSync](src/main/java/bache/counter/CounterServiceSync.java)


PHP
-------
    <?php
    
    require_once('baratine-php/baratine-client.php');
    
    $client = new baratine\BaratineClient('http://localhost:8085/s/pod');
    
    $service = $client->_lookup('/map/123');
    
    $size = $service->put("foo", "bbb");
    $value = $service->get("foo");
    
    // if you want type checking, you can call _as() to create a proxy against
    // your PHP MapService.php interface class that you would provide
    $map = $service->_as('MapService');
    
    $size = $map->put("foo", "ccc");
    $value = $map->get("foo");
    
    // an exception is thrown because doesNotExist() does not exist in your MapService.php class
    $map->doesNotExist(123, 456);

The directory `baratine-php/` is located within the Baratine distribution directory `baratine/modules/`


Bartwit Example Application
===========================
[Bartwit](https://github.com/baratine/bartwit) is a fork of
[Retwis](http://redis.io/topics/twitter-clone) that uses Bache in lieu of Redis.
It serves to demonstrate that:

1. Bache can easily replace Redis, and that
2. you can use Bache/Baratine as your primary datastore instead of a traditional SQL database

Redis commands in Retwis like:

    $r->lpush("timeline",$postid);
    $r->ltrim("timeline",0,1000);

were replaced with:

    lookupList("/list/timeline")->pushHead($postid);
    lookupList("/list/timeline")->trim(0,1000);
    
where `lookupList()` uses the `baratine/modules/baratine-php` client library as follows:

    function lookupList(/* string */ $url)
    {
      return getBaratineClient()->_lookup($url)->_as('\baratine\cache\ListService');
    }

In Bache, `/list` is the parent service and `/list/timeline` is a child service
that shares the parent's inbox.  A call to `pushHead()` would:

1. call into the service's `@OnLookup` annotated method: `ListServiceManagerImpl.onLookup()`
2. `@OnLookup` returns the child instance that would handle the request: `ListServiceImpl`
3. finally Baratine calls the `ListServiceImpl.pushHead()` method

Bartwit vs Retwis Benchmark
---------------------------
For the same number of users and posts on `timeline.php`:

Bartwit: 1 client **1140** requests/sec, 64 clients **2790** requests/sec

Retwis: 1 client **1160** requests/sec, 64 clients **3570** requests/sec

Bartwit shows that for a real world application, Bache (and in turn Baratine) performs very competitively versus Redis. 
The big difference is that Bache is just Java code packaged within a jar file; you can easily extend Bache
with bespoke functionality that better suits your specific application.

How is Bache Implemented
========================
Bache data structures are each a journaled `@Service`:

    @Journal
    @Service("/list")
    public class ListServiceManagerImpl
    
    @Journal
    @Service("/map")
    public class MapServiceManagerImpl
    
    @Journal
    @Service("/tree")
    public class TreeServiceManagerImpl
    
    @Journal
    @Service("/counter")
    public class CounterServiceManagerImpl
    
In Baratine, a service needs to implement `@OnLookup` if it wants to handle child URLs
(e.g. `/list` is the parent and `/list/foo123` is the child).  Otherwise, the caller would get a service-not-found exception.

For `ListServiceManagerImpl`, it's `@OnLookup` simply returns a `ListServiceImpl` instance.  Baratine will cache that
instance in an LRU and perform lifecycle operations as needed.  `ListServiceImpl` participates in the lifecycle operations by
implementing `@OnLoad` and `@OnSave`:

    @OnLoad
    public void onLoad(Result<Boolean> result)
    {
      if (log.isLoggable(Level.FINE)) {
        log.fine(getClass().getSimpleName() + ".onLoad0: id=" + _id);
      }

      getStore().get(_storeKey, result.from(v->onLoadComplete(v)));
    }

    private boolean onLoadComplete(LinkedList<T> list)
    {
      if (list != null) {
        _list = list;
      }
      else {
        _list = null;
      }

      if (log.isLoggable(Level.FINE)) {
        log.fine(getClass().getSimpleName() + ".onLoad1: id=" + _id + " done, list=" + list);
      }

      return true;
    }

    @OnSave
    public void onSave(Result<Boolean> result)
    {
      if (log.isLoggable(Level.FINE)) {
        log.fine(getClass().getSimpleName() + ".onSave0: id=" + _id + ", list=" + _list);
      }

      if (_list != null) {
        getStore().put(_storeKey, _list);
      }
      else {
        // then is deleted
        getStore().remove(_storeKey);
      }

      result.complete(true);

      if (log.isLoggable(Level.FINE)) {
        log.fine(getClass().getSimpleName() + ".onSave1: id=" + _id + " done");
      }
    }

The state of the service is persisted to `io.baratine.core.Store`.  `@OnLoad` is called when:

1. the service instance is being instantiated for the first time, or
2. the service instance has been unloaded and saved, and needs to be loaded back into memory

`@OnSave` is called if any `@Modify` methods have been called at least once and:

1. the journal is going to be flushed and the service instance needs to save its state, or
2. the service is shutting down, or
3. someone requested a save with a call to this service's `io.baratine.core.ServiceRef.save()`

