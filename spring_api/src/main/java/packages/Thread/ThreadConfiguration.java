package packages.Thread;

public record ThreadConfiguration(int corePoolSize, int maxPoolSize, int queueCapacity, long keepAliveTime, String threadNamePrefix) {

    public static ThreadConfiguration defaultConfig() {
        return new ThreadConfiguration(4, 8, 100, 60000, "");
    }
}