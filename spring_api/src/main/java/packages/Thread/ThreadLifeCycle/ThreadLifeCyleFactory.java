package packages.Thread.ThreadLifeCycle;

import packages.Thread.Interfaces.ThreadLifecyleInt;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLifeCyleFactory implements ThreadFactory {
    private final ThreadLifecyleInt listener;
    private final String prefix;
    private final AtomicInteger threadCount = new AtomicInteger(1);

    public ThreadLifeCyleFactory(String prefix, ThreadLifecyleInt listener) {
        this.prefix = prefix;
        this.listener = listener;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(new ThreadLifeCycleRunnable(r, listener));
        thread.setName(prefix + threadCount.getAndIncrement());
        listener.onThreadCreated(thread);
        return thread;
    }
}
