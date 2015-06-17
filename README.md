Bache is a Baratine service that exposes common data structures as a REST service.
Bache provides a map, list, tree, string, and counter type that are callable
from any client supporting WebSockets or HTTP.  You can store any object as
both the key or value field in the map, list, and tree data types.

TODO: persistence and journaling

In many ways, Bache is very similar to [Redis](http://redis.io/); Bache services should be familiar to Redis users.
Bache shows that you can write any kind of Baratine service that runs about as fast as Redis, but with much more
functionality (and without having to resort to Lua scripting).


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
```java
    import io.baratine.core.ResultFuture;
    import com.caucho.amp.hamp.ClientHamp;
    import bache.map.MapService;
    import bache.map.MapServiceSync;

    ClientHamp client = new ClientHamp("http://localhost:8085/s/pod";
    
    MapServiceSync<String,String> mapSync = client.lookup("/map/123").as(MapServiceSync.class);
    System.out.println("value is: " + map.get("foo"));
```

TODO: talk about lookup instances

When you do a lookup(), you may cast the proxy to whatever interface class you want.  The Baratine proxy will do a little
bit of magic to marshal arguments back and forth.  Bache provides both asynchronous and synchronous interfaces for its
services.

URL         | Async API     | Sync API
------------|---------------|---------
/map        | [bache.map.MapService](src/main/java/bache/map/MapService.java) | [bache.map.MapServiceSync](src/main/java/bache/map/MapServiceSync.java)
/list        | [bache.list.ListService](src/main/java/bache/list/ListService.java) | [bache.list.ListServiceSync](src/main/java/bache/list/ListServiceSync.java)
/tree        | [bache.tree.TreeService](src/main/java/bache/tree/TreeService.java) | [bache.tree.TreeServiceSync](src/main/java/bache/tree/TreeServiceSync.java)
/string        | [bache.string.StringService](src/main/java/bache/string/StringService.java) | [bache.string.StringServiceSync](src/main/java/bache/string/StringServiceSync.java)
/counter        | [bache.counter.CounterService](src/main/java/bache/counter/CounterService.java) | [bache.counter.CounterServiceSync](src/main/java/bache/counter/CounterServiceSync.java)


PHP
-------
```php
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
```

The directory `baratine-php/` is located within the Baratine distribution directory `baratine/modules/`


Bartwit Example Application
===========================
[Bartwit](https://github.com/baratine/bartwit) is a fork of
[Retwis](http://redis.io/topics/twitter-clone) that uses Bache in lieu of Redis.
It serves to demonstrate that:

1. Bache can replace Redis, and that
2. you can use Bache/Baratine as your primary datastore instead of a traditional SQL database

Redis commands in Retwis like:

```php
    $r->lpush("timeline",$postid);
    $r->ltrim("timeline",0,1000);
```

were replaced with:

```php
    lookupList("/list/timeline")->pushHead($postid);
    lookupList("/list/timeline")->trim(0,1000);
```

where `lookupList()` uses the `baratine/modules/baratine-php` client library as follows:

```php
    function lookupList(/* string */ $url)
    {
      return getBaratineClient()->_lookup($url)->_as('\baratine\cache\ListService');
    }
```

In Bache, `/list` is the parent service and `/list/timeline` is a child instance
that shares the parent's inbox.  A call to `pushHead()` would:

1. call into the service's `@OnLookup` annotated method: `ListServiceManagerImpl.onLookup()`
2. `@OnLookup` returns the child instance that would handle the request: `ListServiceImpl`
3. finally Baratine calls the `ListServiceImpl.pushHead()` method


Bartwit vs Retwis Benchmark
---------------------------
For the same number of users and posts on `timeline.php`:

|         |   1 client   | 64 clients
--------- | ------------ | -------------------
| Retwis  |  1160 req/s  | 3570 req/s
| Bartwit |  1140 req/s  | 2790 req/s

Bache (and in turn Baratine) performs very competitively versus Redis.   Bache is just Java code
packaged within a jar file and you can easily extend Bache with bespoke functionality that better suits your specific
application.


How is Bache Implemented
========================
Bache data structures are each a journaled `@Service`:

```java
    @Journal
    @Service("/list")
    public class ListManagerServiceImpl
    
    @Journal
    @Service("/map")
    public class MapManagerServiceImpl
    
    @Journal
    @Service("/tree")
    public class TreeManagerServiceImpl
    
    @Journal
    @Service("/string")
    public class StringManagerServiceImpl
    
    @Journal
    @Service("/counter")
    public class CounterManagerServiceImpl
```

In Baratine, a service needs to implement `@OnLookup` if it wants to handle child URLs
(e.g. `/list` is the parent and `/list/foo123` is the child).  Otherwise, the caller would get a service-not-found exception.

For `ListServiceManagerImpl`, its `@OnLookup` simply returns a `ListServiceImpl` instance.  Baratine will cache that
instance in an LRU and perform lifecycle operations as needed.  `ListServiceImpl` participates in the lifecycle operations by
implementing `@OnLoad` and `@OnSave`:

```java
    @OnLoad
    public void onLoad(Result<Boolean> result)
    {
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

      return true;
    }

    @OnSave
    public void onSave(Result<Boolean> result)
    {
      if (_list != null) {
        getStore().put(_storeKey, _list);
      }
      else {
        // then is deleted
        getStore().remove(_storeKey);
      }

      result.complete(true);
    }
```

The state of the service is persisted to `io.baratine.core.Store`.  `@OnLoad` is called when:

1. the service instance is being instantiated for the first time, or
2. the service instance has been unloaded and saved, and needs to be loaded back into memory

`@OnSave` is called if any `@Modify` methods have been called at least once and:

1. the journal is going to be flushed and the service instance needs to save its state, or
2. the service is shutting down, or
3. someone requested a save with a call to this service's `io.baratine.core.ServiceRef.save()`

| Parent                     | Child                |
---------------------------- | -------------------- |
| [MapManagerServiceImpl](src/main/java/bache/map/MapManagerServiceImpl.java)      | [MapServiceImpl](src/main/java/bache/map/MapServiceImpl.java)       |
| [ListManagerServiceImpl](src/main/java/bache/list/ListManagerServiceImpl.java)     | [ListServiceImpl](src/main/java/bache/list/ListServiceImpl.java)      |
| [TreeManagerServiceImpl](src/main/java/bache/tree/TreeManagerServiceImpl.java)     | [TreeServiceImpl](src/main/java/bache/tree/TreeServiceImpl.java)      |
| [StringManagerServiceImpl](src/main/java/bache/string/StringManagerServiceImpl.java)   | [StringServiceImpl](src/main/java/bache/string/StringServiceImpl.java)    |
| [CounterManagerServiceImpl](src/main/java/bache/counter/CounterManagerServiceImpl.java)  | [CounterServiceImpl](src/main/java/bache/counter/CounterServiceImpl.java)   |

In Bache, the parent services are responsible for instantiating the child services (through `@OnLookup`) and handling query
and multi-key delete requests.  The child services handle most of the application logic.  But there is no restriction from
putting more application logic in the parent.


Journaling
----------
TODO: talk about journaling


Example Architecture Diagram (Bartwit)
--------------------------------------
![bartwit diagram](https://github.com/baratine/bache/blob/master/bartwit.png)
