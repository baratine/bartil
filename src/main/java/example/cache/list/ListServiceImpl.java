package example.cache.list;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import io.baratine.core.Modify;
import io.baratine.core.OnLoad;
import io.baratine.core.OnSave;
import io.baratine.core.Result;
import io.baratine.store.Store;

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
    if (index < _list.size()) {
      result.complete(_list.get(index));
    }
    else {
      result.complete(null);
    }
  }

  @Override
  public void getRange(int start, int end, Result<List<T>> result)
  {
    LinkedList<T> list = new LinkedList<>();

    int i = 0;
    for (T value : _list) {
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

    result.complete(list);
  }

  @Override
  public void getAll(Result<List<T>> result)
  {
    result.complete(_list);
  }

  @Modify
  @Override
  @SuppressWarnings("unchecked")
  public void pushHead(T value, Result<Integer> result)
  {
    _list.addFirst(value);

    result.complete(_list.size());
  }

  @Modify
  @Override
  @SuppressWarnings("unchecked")
  public void pushTail(T value, Result<Integer> result)
  {
    _list.addLast(value);

    result.complete(_list.size());
  }

  @Modify
  @Override
  @SuppressWarnings("unchecked")
  public void pushHeadMultiple(Result<Integer> result, T ... values)
  {
    for (T value : values) {
      _list.addFirst(value);
    }

    result.complete(_list.size());
  }

  @Modify
  @Override
  @SuppressWarnings("unchecked")
  public void pushTailMultiple(Result<Integer> result, T ... values)
  {
    for (T value : values) {
      _list.addLast(value);
    }

    result.complete(_list.size());
  }

  @Modify
  @Override
  public void popHead(Result<T> result)
  {
    result.complete(_list.pollFirst());
  }

  @Modify
  @Override
  public void popTail(Result<T> result)
  {
    result.complete(_list.pollLast());
  }

  @Modify
  @Override
  public void set(int index, T value, Result<Boolean> result)
  {
    if (0 <= index && index < _list.size()) {
      _list.set(index, value);

      result.complete(true);
    }
    else {
      result.complete(false);
    }
  }

  @Modify
  @Override
  public void remove(T value, int count, Result<Integer> result)
  {
    int removeCount = 0;

    if (count == 0) {
      count = Integer.MIN_VALUE;
    }

    if (count > 0) {
      Iterator<T> iter = _list.listIterator();

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
      Iterator<T> iter = _list.descendingIterator();

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
      start += _list.size();
    }

    if (end < 0) {
      end += _list.size();
    }

    end = Math.min(end, _list.size());

    while (_list.size() > end) {
      _list.removeLast();
    }

    while (start-- > 0) {
      _list.removeFirst();
    }

    result.complete(_list.size());
  }

  @Modify
  @Override
  public void clear(Result<Integer> result)
  {
    int size = _list.size();

    _list.clear();

    result.complete(size);
  }

  @Override
  public void size(Result<Integer> result)
  {
    result.complete(_list.size());
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

  private Store<LinkedList<T>> getStore()
  {
    return _store;
  }
}
