package packages.ORM.repository;


import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class RepositoryServiceHandler {
    private static final Map<Class<?>, Object> REPOSITORY_CACHE = new HashMap<>();

    public static void injectRepositories(Object target) {
        Class<?> targetClass = target.getClass();

        for (Field field : targetClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(SetUpRepo.class)) {
                try {
                    field.setAccessible(true);
                    Class<?> repositoryInterface = field.getType();
                    Object repository = getOrCreateRepository(repositoryInterface);
                    field.set(target, repository);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject repository into " + field.getName(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getOrCreateRepository(Class<T> repositoryInterface) {
        return (T) REPOSITORY_CACHE.computeIfAbsent(repositoryInterface, repoInterface -> {
            return Proxy.newProxyInstance(
                    repoInterface.getClassLoader(),
                    new Class<?>[] { repoInterface },
                    new RepositoryInvocationHandler(repoInterface)
            );
        });
    }
}
