package bartil.string;

import io.baratine.core.Result;
import io.baratine.core.ResultStream;

public interface StringService
{
  void get(Result<String> result);

  void set(String value, Result<Boolean> result);

  void delete(Result<Boolean> result);

  void exists(Result<Boolean> result);
}
