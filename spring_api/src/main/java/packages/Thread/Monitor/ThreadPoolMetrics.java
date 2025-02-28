package packages.Thread.Monitor;

public record ThreadPoolMetrics(
        int currentPoolSize,
        int corePoolSize,
        int maxPoolSize,
        int activeThreads,
        long completedTasks,
        int queueSize
){};
