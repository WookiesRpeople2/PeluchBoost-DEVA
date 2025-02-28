package packages.Thread.ThreadLifeCycle;

import packages.Thread.Interfaces.ThreadLifecyleInt;

public class ThreadLifecycleImpl implements ThreadLifecyleInt {
    @Override
    public void onThreadCreated(Thread thread) {
        System.out.println("Thread created: " + thread.getName());
    }

    @Override
    public void onThreadStarted(Thread thread) {
        System.out.println("Thread started: " + thread.getName());
    }

    @Override
    public void onThreadTerminated(Thread thread) {
        System.out.println("Thread terminated: " + thread.getName());
    }
}
