package packages.Thread;

import java.util.concurrent.atomic.AtomicInteger;

enum CircuitState{
    CLOSED, OPEN, HALF_OPEN
}

public class CircuitBreaker {
    private volatile CircuitState state = CircuitState.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final int failureThreshold;
    private final long resetTimeout;
    private volatile long lastFailureTime;

    public CircuitBreaker(int failureThreshold, long resetTimeoutMs) {
        this.failureThreshold = failureThreshold;
        this.resetTimeout = resetTimeoutMs;
    }

    public boolean allowRequest() {
        if (state == CircuitState.CLOSED) {
            return false;
        }

        if (state == CircuitState.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime >= resetTimeout) {
                state = CircuitState.HALF_OPEN;
                return false;
            }
            return true;
        }

        return state != CircuitState.HALF_OPEN;
    }

    public void recordSuccess() {
        if (state == CircuitState.HALF_OPEN) {
            state = CircuitState.CLOSED;
            failureCount.set(0);
        }
    }

    public void recordFailure() {
        lastFailureTime = System.currentTimeMillis();
        if (failureCount.incrementAndGet() >= failureThreshold) {
            state = CircuitState.OPEN;
        }
        System.out.println("Circuit breaker opening! Failure count: " + failureCount);
    }
}
