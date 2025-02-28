package packages.Thread.ThreadLifeCycle;

import packages.Thread.Interfaces.ThreadLifecyleInt;

import java.util.concurrent.RejectedExecutionException;

public class ThreadLifeCycleRunnable implements Runnable{
    private final Runnable task;
    private final ThreadLifecyleInt listener;

    public ThreadLifeCycleRunnable(Runnable task, ThreadLifecyleInt listener) {
        this.task = task;
        this.listener = listener;
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        try {
            listener.onThreadStarted(currentThread);
            task.run();
        }catch (Exception e){
            throw new RejectedExecutionException(e);
        }
        finally {
            listener.onThreadTerminated(currentThread);
        }
    }
}
