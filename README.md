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

* [/map](https://github.com/baratine/bache/blob/master/src/main/java/bache/map/MapService.java)
* [/list](https://github.com/baratine/bache/blob/master/src/main/java/bache/list/ListService.java)
* [/tree](https://github.com/baratine/bache/blob/master/src/main/java/bache/tree/TreeService.java)
* [/string](https://github.com/baratine/bache/blob/master/src/main/java/bache/string/StringService.java)
* [/counter](https://github.com/baratine/bache/blob/master/src/main/java/bache/counter/CounterService.java)

To call the /map service for example (assuming you deployed it to the default 
pod):

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

PHP
-------
    <?php
    
    require_once('baratine-php/baratine-client.php');
    
    $client = new BaratineClient('http://localhost:8085/s/pod');
    
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

The directory `baratine-php/` is located within the baratine directory `baratine/modules/`


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
    
where `lookupList()` uses baratine/modules/baratine-php client library as follows:

    function lookupList(/* string */ $url)
    {
      return getBaratineClient()->_lookup($url)->_as('\baratine\cache\ListService');
    }

In Bache, `/list` is the parent service and `/list/timeline` is a child service
that shares the parent's inbox.  A call to pushHead() would entail:

1. call into ListServiceManagerImpl's `@OnLookup` annotated method
2. `@OnLookup` returns the child instance that would handle the request, which
  in this case is ListServiceImpl
3. Baratine calls `ListServiceImpl.pushHead()` method
