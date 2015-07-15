package bache.list;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.core.ResultStream;
import io.baratine.store.Store;
import io.baratine.stream.StreamBuilder;

public class ListServiceImpl<T> implements ListService<T>
{
  private static final Logger log
    = Logger.getLogger(ListServiceImpl.class.getName());

  private Store<LinkedList<T>> _store;

  private String _id;
  private String _storeKey;
  private LinkedList<T> _list;

  public ListServiceImpl(String id, String storeKey, Store<LinkedList<T>> store)
  {
    _id = id;
    _storeKey = storeKey;

    _store = store;
  }

  @Override
  public void get(int index, Result<T> result)
  {
    if (_list == null) {
      throw new NullPointerException("list is null");
    }
    else if (index >= _list.size()) {
      throw new IndexOutOfBoundsException();
    }

    result.complete(_list.get(index));
  }

  @Override
  public void getRange(int start, int end, Result<List<T>> result)
  {
    LinkedList<T> list = new LinkedList<>();

    if (_list != null) {
      int i = 0;
      for (T value : getList()) {
        int j = i++;

        if (j < start) {
          continue;
        }
        else if (j >= end) {
          break;
        }
        else {
          list.addLast(value);
        }
      }
    }

    result.complete(list);
  }

  @Override
  public void getAll(Result<List<T>> result)
  {
    LinkedList<T> list = new LinkedList<>();

    if (_list != null) {
      list.addAll(_list);
    }

    result.complete(list);
  }

  @Modify
  @Override
  @SuppressWarnings("unchecked")
  public void pushHead(T value, Result<Integer> result)
  {
    getList().addFirst(value);

    result.complete(getList().size());
  }

  @Modify
  @Override
  @SuppressWarnings("unchecked")
  public void pushTail(T value, Result<Integer> result)
  {
    getList().addLast(value);

    result.complete(getList().size());
  }

  @Modify
  @Override
  @SuppressWarnings("unchecked")
  public void pushHeadMultiple(Result<Integer> result, T ... values)
  {
    for (T value : values) {
      getList().addFirst(value);
    }

    result.complete(getList().size());
  }

  @Modify
  @Override
  @SuppressWarnings("unchecked")
  public void pushTailMultiple(Result<Integer> result, T ... values)
  {
    for (T value : values) {
      getList().addLast(value);
    }

    result.complete(getList().size());
  }

  @Modify
  @Override
  public void popHead(Result<T> result)
  {
    result.complete(getList().pollFirst());
  }

  @Modify
  @Override
  public void popTail(Result<T> result)
  {
    result.complete(getList().pollLast());
  }

  @Modify
  @Override
  public void set(int index, T value, Result<Boolean> result)
  {
    if (0 <= index && index < getList().size()) {
      getList().set(index, value);

      result.complete(true);
    }
    else {
      result.complete(false);
    }
  }

  @Modify
  @Override
  public void remove(int index, Result<Integer> result)
  {
    int removeCount = 0;

    if (0 <= index && index <= getList().size()) {
      getList().remove(index);

      removeCount = 1;
    }

    result.complete(removeCount);
  }

  @Modify
  @Override
  public void removeValue(T value, int count, Result<Integer> result)
  {
    int removeCount = 0;

    if (count == 0) {
      count = Integer.MIN_VALUE;
    }

    if (count > 0) {
      Iterator<T> iter = getList().listIterator();

      while (count > 0 && iter.hasNext()) {
        T node = iter.next();

        if (value.equals(node)) {
          iter.remove();
          count--;

          removeCount++;
        }
      }
    }
    else {
      Iterator<T> iter = getList().descendingIterator();

      while (count < 0 && iter.hasNext()) {
        T node = iter.next();

        if (value.equals(node)) {
          iter.remove();
          count++;

          removeCount++;
        }
      }
    }

    result.complete(removeCount);
  }

  @Modify
  @Override
  public void trim(int start, int end, Result<Integer> result)
  {
    if (start < 0) {
      start += getList().size();
    }

    if (end < 0) {
      end += getList().size();
    }

    end = Math.min(end, getList().size());

    while (getList().size() > end) {
      getList().removeLast();
    }

    while (start-- > 0 && getList().size() > 0) {
      getList().removeFirst();
    }

    result.complete(getList().size());
  }

  @Modify
  @Override
  public void clear(Result<Integer> result)
  {
    int size = getList().size();

    getList().clear();

    result.complete(size);
  }

  @Override
  public void size(Result<Integer> result)
  {
    int size = 0;

    if (_list != null) {
      size = _list.size();
    }

    result.complete(size);
  }

  @Modify
  @Override
  public void delete(Result<Boolean> result)
  {
    if (_list != null) {
      _list.clear();

      _list = null;
    }

    result.complete(true);
  }

  @Override
  public void exists(Result<Boolean> result)
  {
    result.complete(_list != null);
  }

  @Override
  public StreamBuilder<T> stream(int offset, int length)
  {
    throw new IllegalStateException();
  }

  public void stream(ResultStream<T> sink, int start, int end)
  {
    end = Math.min(end, _list.size());

    int i = 0;
    for (T value : _list) {
      if (i < start) {
      }
      else if (i >= end) {
        break;
      }
      else {
        sink.accept(value);
      }

      i++;
    }

    sink.complete();
  }

  @OnLoad
  public void onLoad(Result<Boolean> result)
  {
    if (log.isLoggable(Level.FINE)) {
      log.fine(getClass().getSimpleName() + ".onLoad0: id=" + _id);
    }

    getStore().get(_storeKey, result.from(list -> onLoadComplete(list)));
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

  private LinkedList<T> getList()
  {
    if (_list == null) {
      _list = new LinkedList<>();
    }

    return _list;
  }

  private Store<LinkedList<T>> getStore()
  {
    return _store;
  }
}
