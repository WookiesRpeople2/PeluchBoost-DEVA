package packages.Thread.Async;

import packages.Thread.ThreadConfiguration;
import packages.Thread.ThreadPool;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AsyncPool extends ThreadPool {

    public  AsyncPool(ThreadConfiguration config){
        super(config);
    }

    public AsyncPool(){
        super();
    }

    public <T> Async<T> submitAsync(Supplier<T> task) {
        return submitAsync(task, 5);
    }

    public <T> Async<T> submitAsync(Supplier<T> task, int priority) {
        CompletableFuture<T> future = new CompletableFuture<>();

        execute(() -> {
            try {
                T result = task.get();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, priority);

        return new Async<>(future, priority);
    }

    public Async<Void> executeAsync(Runnable task) {
        return executeAsync(task, 5);
    }

    public Async<Void> executeAsync(Runnable task, int priority) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        execute(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, priority);

        return new Async<>(future, priority);
    }

    public <T> Async<List<T>> submitAllAsync(List<Supplier<T>> tasks) {
        return submitAllAsync(tasks, 5);
    }

    public <T> Async<List<T>> submitAllAsync(List<Supplier<T>> tasks, int priority) {
        List<Async<T>> futures = tasks.stream()
                .map(task -> submitAsync(task, priority))
                .toList();

        CompletableFuture<List<T>> combinedFuture = CompletableFuture.allOf(
                futures.stream()
                        .map(Async::getFuture)
                        .toArray(CompletableFuture[]::new)
        ).thenApply(v -> futures.stream()
                .map(Async::getFuture)
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));

        return new Async<>(combinedFuture, priority);
    }
}
