package packages.ORM;

import packages.ORM.config.DatabaseConfig;
import packages.ORM.annotations.Column;
import packages.ORM.annotations.Entity;
import packages.ORM.annotations.Relationship;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class EntityManager {
    private final Connection connection;

    public EntityManager() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) throws Exception {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnnotation.tableName().isEmpty() ?
                entityClass.getSimpleName().toLowerCase() : entityAnnotation.tableName();

        String query = "SELECT * FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, primaryKey);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEntity(rs, entityClass);
            }
        }

        return null;
    }

    public <T> void persist(T entity) throws Exception {
        Class<?> entityClass = entity.getClass();
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnnotation.tableName().isEmpty() ?
                entityClass.getSimpleName().toLowerCase() : entityAnnotation.tableName();
        Map<String, Object> values = new HashMap<>();
        Field idField = getIdField(entityClass);
        idField.setAccessible(true);

        for (Field field : entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(Column.class) && !field.equals(idField)) {
                    Object value = field.get(entity);
                    if (value != null) {
                        Column column = field.getAnnotation(Column.class);
                        String columnName = column.name().isEmpty() ? field.getName() : column.name();
                        values.put(columnName, value);
                    }
                } else if (field.isAnnotationPresent(Relationship.class)) {
                    Object relatedEntity = field.get(entity);
                    if (relatedEntity != null) {
                        Field relatedIdField = getIdField(relatedEntity.getClass());
                        relatedIdField.setAccessible(true);
                        values.put(field.getName() + "_id", relatedIdField.get(relatedEntity));
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        String idColumnName = idField.getName();
        Column idColumnAnnotation = idField.getAnnotation(Column.class);
        if (idColumnAnnotation != null && !idColumnAnnotation.name().isEmpty()) {
            idColumnName = idColumnAnnotation.name();
        }

        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder placeholders = new StringBuilder(") VALUES (");
        List<Object> paramValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!paramValues.isEmpty()) {
                sql.append(", ");
                placeholders.append(", ");
            }
            sql.append(entry.getKey());
            placeholders.append("?");
            paramValues.add(entry.getValue());
        }

        sql.append(placeholders).append(")");

        connection.setAutoCommit(false);
        try {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET @last_inserted_uuid = UUID()");
            }

            StringBuilder insertWithUUID = new StringBuilder("INSERT INTO " + tableName + " (" + idColumnName);
            StringBuilder valuesWithUUID = new StringBuilder(" VALUES (@last_inserted_uuid");

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                insertWithUUID.append(", ").append(entry.getKey());
                valuesWithUUID.append(", ?");
            }

            insertWithUUID.append(")").append(valuesWithUUID).append(")");

            try (PreparedStatement stmt = connection.prepareStatement(insertWithUUID.toString())) {
                for (int i = 0; i < paramValues.size(); i++) {
                    stmt.setObject(i + 1, paramValues.get(i));
                }
                stmt.executeUpdate();
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT @last_inserted_uuid")) {
                if (rs.next()) {
                    String uuid = rs.getString(1);
                    idField.set(entity, uuid);
                    System.out.println("Generated UUID: " + uuid);
                }
            }

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public <T> void update(T entity) throws Exception {
        Class<?> entityClass = entity.getClass();
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnnotation.tableName().isEmpty() ?
                entityClass.getSimpleName().toLowerCase() : entityAnnotation.tableName();

        List<Field> fields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class))
                .toList();

        String updates = fields.stream()
                .map(f -> {
                    String columnName = f.getAnnotation(Column.class).name().isEmpty() ?
                            f.getName() : f.getAnnotation(Column.class).name();
                    return columnName + " = ?";
                })
                .collect(Collectors.joining(", "));

        String query = "UPDATE " + tableName + " SET " + updates + " WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < fields.size(); i++) {
                fields.get(i).setAccessible(true);
                stmt.setObject(i + 1, fields.get(i).get(entity));
            }

            Field idField = fields.stream()
                    .filter(f -> f.getAnnotation(Column.class).primaryKey())
                    .findFirst()
                    .orElseThrow();
            idField.setAccessible(true);
            stmt.setObject(fields.size() + 1, idField.get(entity));

            stmt.executeUpdate();
        }
    }

    public <T> void delete(T entity) throws Exception {
        Class<?> entityClass = entity.getClass();
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnnotation.tableName().isEmpty() ?
                entityClass.getSimpleName().toLowerCase() : entityAnnotation.tableName();

        Field idField = Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class) &&
                        f.getAnnotation(Column.class).primaryKey())
                .findFirst()
                .orElseThrow();

        String query = "DELETE FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            idField.setAccessible(true);
            stmt.setObject(1, idField.get(entity));
            stmt.executeUpdate();
        }
    }

    public <T> List<T> findAll(Class<T> entityClass) throws Exception {
        try {
            Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
            String tableName = entityAnnotation.tableName().isEmpty() ?
                    entityClass.getSimpleName().toLowerCase() : entityAnnotation.tableName();

            String query = "SELECT * FROM " + tableName;
            List<T> results = new ArrayList<>();

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs, entityClass));
                }
            }

            return results;
        }catch(Exception e){
            throw new Exception(e);
        }
    }

    public <T> T mapResultSetToEntity(ResultSet rs, Class<T> entityClass) throws Exception {
        T entity;
        try {
            entity = entityClass.getDeclaredConstructor().newInstance();
        }catch (Exception e){
            entity = createInstanceWithoutConstructor(entityClass);
        }

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name().isEmpty() ? field.getName() : column.name();

                field.setAccessible(true);
                field.set(entity, rs.getObject(columnName));
            }
        }

        return entity;
    }


    private Field getIdField(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column.primaryKey()) {
                return field;
            }
        }
        throw new RuntimeException("No ID field found for entity " + entityClass.getName());
    }

    private  <T> T createInstanceWithoutConstructor(Class<T> clazz) throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);

        return (T) unsafe.allocateInstance(clazz);
    }


}
