package packages.Thread.Backpressure;

import java.util.concurrent.Semaphore;

import packages.Thread.PrioritizedTask;
import packages.Thread.Interfaces.TaskExecutionHandler;

public class BackpressureRunnable extends PrioritizedTask{
    private final Semaphore semaphore;
    private final TaskExecutionHandler handler; 

    public BackpressureRunnable(Runnable task, int priority, Semaphore semaphore, TaskExecutionHandler handler ) {
        super(task, priority);
        this.semaphore = semaphore;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            super.run();
            handler.onSuccess();
        }catch (Exception e){
            handler.onFailure(e);
        }
        finally {
            semaphore.release();
        }
    }
}
