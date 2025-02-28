package packages.Thread.Interfaces;


public interface TaskExecutionHandler{
  void onSuccess();
  void onFailure(Exception e);
}
