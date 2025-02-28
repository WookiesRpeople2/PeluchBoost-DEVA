package packages.Thread.Interfaces;

import packages.Thread.Monitor.ThreadPoolMetrics;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ExecuterInt {
    void execute(Runnable task);
    <T> Future<T> submit(Callable<T> task);
    void shutdown();
    ThreadPoolMetrics getMetrics();
}



