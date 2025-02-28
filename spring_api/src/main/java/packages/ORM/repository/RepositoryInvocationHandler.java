package packages.ORM.repository;

import packages.ORM.config.DatabaseConfig;
import packages.ORM.EntityManager;
import packages.ORM.records.QueryMetadata;

import java.lang.reflect.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepositoryInvocationHandler implements InvocationHandler {
    private final EntityManager entityManager;
    private final Class<?> entityClass;
    private static final Pattern QUERY_PATTERN = Pattern.compile(
            "^(find|findAll|count|delete|save|update|exists)(?:By([A-Z][a-zA-Z0-9]*(?:And[A-Z][a-zA-Z0-9]*)*))?$"
    );
    private static final Pattern ID_PATTERN = Pattern.compile("(.+)Id$");

    public RepositoryInvocationHandler(Class<?> repositoryInterface) {
        this.entityManager = new EntityManager();
        this.entityClass = extractEntityClass(repositoryInterface);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }

        QueryMetadata queryMetadata = parseMethodName(method.getName());
        if (queryMetadata != null) {
            return executeQuery(queryMetadata, method.getReturnType(), args);
        }

        throw new UnsupportedOperationException("Method not supported: " + method.getName());
    }


    private QueryMetadata parseMethodName(String methodName) {
        Matcher matcher = QUERY_PATTERN.matcher(methodName);
        System.out.println(matcher);
        if (!matcher.matches()) {
            return null;
        }

        String operation = matcher.group(1);
        List<String> properties = new ArrayList<>();
        if (matcher.group(2) != null) {
            String[] propertyNames = matcher.group(2).split("And");
            for (String prop : propertyNames) {
                properties.add(convertToColumnName(prop.substring(0, 1).toLowerCase() + prop.substring(1)));
            }
        }
        System.out.println(operation + properties);

        return new QueryMetadata(operation, properties);
    }

    private Object executeQuery(QueryMetadata metadata, Class<?> returnType, Object[] args) throws Exception {
        if (metadata.operation().equals("save")) {
             entityManager.persist(args[0]);
             return args[0];
        }
        if(metadata.operation().equals("update")){
            entityManager.update(args[0]);
            return args[0];
        }

        String sql = generateSql(metadata);

        try (PreparedStatement stmt = DatabaseConfig.getInstance().getConnection().prepareStatement(sql)) {
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    stmt.setObject(i + 1, args[i]);
                    System.out.println("Setting parameter " + (i + 1) + " to: " + args[i]);
                }
            }

            ResultSet rs = stmt.executeQuery();
            return handleResults(rs, metadata, returnType);
        }
    }

    private String generateSql(QueryMetadata metadata) {
        String tableName = entityClass.getSimpleName().toLowerCase();
        StringBuilder sql = new StringBuilder();

        switch (metadata.operation()) {
            case "findAll", "find" -> sql.append("SELECT * FROM ").append(tableName);
            case "count" -> sql.append("SELECT COUNT(*) FROM ").append(tableName);
            case "exists" -> sql.append("SELECT EXISTS(SELECT 1 FROM ").append(tableName);
            case "delete" -> sql.append("DELETE FROM ").append(tableName);
        }

        if (!metadata.properties().isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < metadata.properties().size(); i++) {
                if (i > 0) sql.append(" AND ");
                sql.append(metadata.properties().get(i)).append(" = ?");
            }
        }

        if (metadata.operation().equals("exists")) {
            sql.append(")");
        }
        System.out.println(sql.toString());

        return sql.toString();
    }

    private Object handleResults(ResultSet rs, QueryMetadata metadata, Class<?> returnType) throws Exception {
        switch (metadata.operation()) {
            case "findAll" -> {
                if (returnType == List.class) {
                    List<Object> results = new ArrayList<>();
                    while (rs.next()) {
                        results.add(entityManager.mapResultSetToEntity(rs, entityClass));
                    }
                    return results;
                } else {
                    return rs.next() ? entityManager.mapResultSetToEntity(rs, entityClass) : null;
                }
            }
            case "find" -> {
                if (rs.next()) {
                    Object result = entityManager.mapResultSetToEntity(rs, entityClass);
                    if (returnType == Optional.class) {
                        return Optional.of(result);
                    }
                    return result;
                }
                return returnType == Optional.class ? Optional.empty() : null;
        }
            case "count" -> {
                return rs.next() ? rs.getLong(1) : 0L;
            }
            case "exists" -> {
                return rs.next() && rs.getBoolean(1);
            }
            case "delete" -> {
                return null;
            }
            default -> throw new UnsupportedOperationException("Unsupported operation: " + metadata.operation());
        }
    }

    private Class<?> extractEntityClass(Class<?> repositoryInterface) {
        Type[] genericInterfaces = repositoryInterface.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType parameterizedType) {
                if (parameterizedType.getRawType() == BaseRepository.class) {
                    return (Class<?>) parameterizedType.getActualTypeArguments()[0];
                }
            }
        }
        throw new IllegalArgumentException("Could not determine entity class");
    }

    private String convertToColumnName(String propertyName) {
        Matcher idMatcher = ID_PATTERN.matcher(propertyName);
        if (idMatcher.matches()) {
            String prefix = idMatcher.group(1);

            StringBuilder snakeCasePrefix = new StringBuilder();
            for (char c : prefix.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    snakeCasePrefix.append('_').append(Character.toLowerCase(c));
                } else {
                    snakeCasePrefix.append(c);
                }
            }

            return snakeCasePrefix.toString() + "_id";
        }

        StringBuilder result = new StringBuilder();
        for (char c : propertyName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }


}

