package packages.ORM;

import packages.ORM.annotations.Entity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class EntityScanner {
    private final String basePackage;
    private final ClassLoader classLoader;

    public EntityScanner(String basePackage) {
        this.basePackage = basePackage;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public Set<Class<?>> scanForEntities() {
        Set<Class<?>> entities = new HashSet<>();
        try {
            String path = basePackage.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                findEntities(new File(resource.getFile()), basePackage, entities);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error scanning for entities", e);
        }
        return entities;
    }

    private void findEntities(File directory, String packageName, Set<Class<?>> entities) throws ClassNotFoundException {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findEntities(file, packageName + "." + file.getName(), entities);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." +
                        file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Entity.class)) {
                        entities.add(clazz);
                    }
            }
        }
    }
}
