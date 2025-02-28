package packages.Thread.Interfaces;

public interface ThreadLifecyleInt {
    void onThreadCreated(Thread thread);
    void onThreadStarted(Thread thread);
    void onThreadTerminated(Thread thread);
}
