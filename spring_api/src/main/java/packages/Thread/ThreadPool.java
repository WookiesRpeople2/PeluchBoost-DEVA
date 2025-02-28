package packages.Thread;

import packages.Thread.Backpressure.BackpressurePoolExecuter;
import packages.Thread.Interfaces.ExecuterInt;
import packages.Thread.Interfaces.TaskExecutionHandler;
import packages.Thread.Interfaces.ThreadLifecyleInt;
import packages.Thread.Monitor.ThreadPoolMetrics;
import packages.Thread.ThreadLifeCycle.ThreadLifeCyleFactory;
import packages.Thread.ThreadLifeCycle.ThreadLifecycleImpl;

import java.util.concurrent.*;

public class ThreadPool implements ExecuterInt {
    private final BackpressurePoolExecuter executor;
    private final CircuitBreaker circuitBreaker;


    public ThreadPool(ThreadConfiguration config) {
        this(config, new PriorityBlockingQueue<>(), new ThreadLifecycleImpl());
    }

    public ThreadPool() {
        this(ThreadConfiguration.defaultConfig(), new PriorityBlockingQueue<>(), new ThreadLifecycleImpl());
    }

    private ThreadPool(ThreadConfiguration config, PriorityBlockingQueue<Runnable> taskQueue, ThreadLifecyleInt lifecycleListener) {
        this.circuitBreaker = new CircuitBreaker(5, 30000);

        ThreadFactory lifecycleAwareFactory = new ThreadLifeCyleFactory(
                config.threadNamePrefix(),
                lifecycleListener
        );

        this.executor = new BackpressurePoolExecuter(
                config.corePoolSize(),
                config.maxPoolSize(),
                config.keepAliveTime(),
                TimeUnit.MILLISECONDS,
                taskQueue,
                lifecycleAwareFactory,
                config.queueCapacity(),
                new TaskExecutionHandler() {
                    @Override
                    public void onSuccess() {
                        circuitBreaker.recordSuccess();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        circuitBreaker.recordFailure();
                    }
                }
        );
    }

    @Override
    public void execute(Runnable task) {
        execute(task, 5);
    }

    public void execute(Runnable task, int priority) {
        if (circuitBreaker.allowRequest()) {
            throw new RejectedExecutionException("Circuit breaker is open");
        }

        executor.execute(new PrioritizedTask(task, priority));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (circuitBreaker.allowRequest()) {
            throw new RejectedExecutionException("Circuit breaker is open");
        }

        return executor.submit(task);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public ThreadPoolMetrics getMetrics() {
        return new ThreadPoolMetrics(
                executor.getPoolSize(),
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getActiveCount(),
                executor.getCompletedTaskCount(),
                executor.getQueue().size()
        );
    }
}
