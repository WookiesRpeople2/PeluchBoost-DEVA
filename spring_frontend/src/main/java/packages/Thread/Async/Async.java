package packages.Thread.Async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class Async<T> {
    private final CompletableFuture<T> future;
    private final int priority;

    public Async(CompletableFuture<T> future, int priority) {
        this.future = future;
        this.priority = priority;
    }

    public T get() throws ExecutionException, InterruptedException {
        return future.get();
    }

    public T get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        return future.get(timeout, unit);
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }


    public <U> Async<U> thenApply(Function<? super T, ? extends U> fn) {
        return new Async<>(future.thenApply(fn), priority);
    }

    public Async<Void> thenAccept(Consumer<? super T> action) {
        return new Async<>(future.thenAccept(action), priority);
    }

    public Async<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return new Async<>(future.exceptionally(fn), priority);
    }

}
