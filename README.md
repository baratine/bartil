Bache is a Baratine service that exposes common data structures as a service.
Bache provides a map, list, tree, string, and counter type that are callable
from any client supporting WebSockets or HTTP.  You can store any object as
both the key or value field in the map, list, and tree data types.

In many ways, Bache is very similar to [Redis](http://redis.io/) and Bache
services should be familiar to Redis users.  Bache is used to show
that you can write any kind of Baratine service that runs about as fast as Redis, but
with much more functionality (and without having to resort to scripting, as is
the case with Redis).

Usage
==========
Bache provices the following services:

* [/map](https://github.com/baratine/bache/blob/master/src/main/java/example/cache/map/MapService.java)
* [/list](https://github.com/baratine/bache/blob/master/src/main/java/example/cache/list/ListService.java)
* [/tree](https://github.com/baratine/bache/blob/master/src/main/java/example/cache/tree/TreeService.java)
* [/string](https://github.com/baratine/bache/blob/master/src/main/java/example/cache/string/StringService.java)
* [/counter](https://github.com/baratine/bache/blob/master/src/main/java/example/cache/counter/CounterService.java)

To call the /map service for example (assuming you deployed it to the default 
pod):

Java
------
    import io.baratine.core.ResultFuture;
    import com.caucho.amp.hamp;
    import example.cache.map.MapService;

    ClientHamp client = new ClientHamp("http://localhost:8085/s/pod";
    
    MapService<String,String> service = client.lookup("/map/123").as(MapService.class);
    
    ResultFuture<String> valueFuture = new ResultFuture<>();

    // calling it asynchronously    
    map.put("foo", "aaa", /* int */ size -> {
      System.out.println("new size is: " + size);
    });
    
    Thread.sleep(2000);
    
    // calling it synchronously
    map.get("foo", valueFuture);
    System.out.println("value is: " + valueFuture.get());

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