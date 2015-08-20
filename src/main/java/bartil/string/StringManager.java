package bartil.string;

import java.util.List;

import io.baratine.core.Result;
import io.baratine.core.ResultStream;
import io.baratine.stream.ResultStreamBuilder;

public interface StringManager
{
  //void delete(Result<Integer> result, String ... keys);

  ResultStreamBuilder<String> findValuesStream(String regexp);

  void findValuesStream(String regexp, ResultStream<String> result);
}
