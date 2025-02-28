package packages.Thread.Backpressure;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import packages.Thread.PrioritizedTask;
import packages.Thread.Interfaces.TaskExecutionHandler;

public class BackpressurePoolExecuter extends ThreadPoolExecutor {
    private final Semaphore semaphore;
    private final TaskExecutionHandler handler;

    public BackpressurePoolExecuter(int corePoolSize,
                                    int maxPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue,
                                    ThreadFactory threadFactory,
                                    int maxConcurrentTasks,
                                    TaskExecutionHandler handler) {
        super(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.semaphore = new Semaphore(maxConcurrentTasks);
        this.handler = handler;
    }

    @Override
    public void execute(Runnable command) {
        try {
            boolean acquired = semaphore.tryAcquire(5, TimeUnit.SECONDS);
            if (acquired) {
                Runnable finalTask = new BackpressureRunnable(
                        ((PrioritizedTask)command).getTask(),
                        ((PrioritizedTask) command).getPriority(),
                        semaphore,
                        this.handler
                );
                super.execute(finalTask);
            } else {
                throw new RejectedExecutionException("Backpressure: Too many concurrent tasks");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RejectedExecutionException("Interrupted while waiting for permit", e);
        }
    }
}
