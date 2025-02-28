package packages.ORM;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import packages.ORM.annotations.Column;
import packages.ORM.annotations.Entity;
import packages.ORM.annotations.Relationship;
import packages.ORM.records.ColumnMetaData;
import packages.ORM.records.TableMetaData;

public class SchemaManager {
    private final Connection connection;
    private final Map<String, TableMetaData> currentSchema;
    private final EntityScanner entityScanner;
    private static SchemaManager instance;

    private SchemaManager(Connection connection, String basePackage) {
        this.connection = connection;
        this.currentSchema = new HashMap<>();
        this.entityScanner = new EntityScanner(basePackage);
        initializeSchema();
    }


    public static void initialize(Connection connection, String basePackage) {
        if (instance == null) {
            instance = new SchemaManager(connection, basePackage);
        }
    }

    public static SchemaManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SchemaManager not initialized. Call initialize() first.");
        }
        return instance;
    }

    private void initializeSchema() {
        try {
            Set<Class<?>> entities = entityScanner.scanForEntities();
            loadCurrentSchema();


            Map<String, TableMetaData> baseSchema = generateBaseSchema(entities);

            List<String> baseMigrations = generateMigrations(currentSchema, baseSchema);


            baseMigrations = baseMigrations.stream()
                    .filter(sql -> !sql.toUpperCase().contains("MODIFY COLUMN"))
                    .collect(Collectors.toList());

            executeMigrations(baseMigrations);


            Map<String, TableMetaData> completeSchema = addForeignKeyRelationships(entities, baseSchema);
            List<String> relationshipMigrations = generateForeignKeyMigrations(baseSchema, completeSchema);
            executeMigrations(relationshipMigrations);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize schema", e);
        }
    }

    private void executeMigrations(List<String> migrations) {
        for (String migration : migrations) {
            try (var statement = connection.createStatement()) {
                statement.execute(migration);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to execute migration: " + migration, e);
            }
        }
    }

    private Map<String, TableMetaData> generateBaseSchema(Set<Class<?>> entities) {
        Map<String, TableMetaData> baseSchema = new HashMap<>();

        for (Class<?> entityClass : entities) {
            Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
            String tableName = entityAnnotation.tableName().isEmpty() ?
                    entityClass.getSimpleName().toLowerCase() : entityAnnotation.tableName();

            List<ColumnMetaData> columnMetaDataList = new ArrayList<>();

            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    ColumnMetaData columnMetadata = getColumnMetaData(field);
                    columnMetaDataList.add(columnMetadata);
                }
            }

            baseSchema.put(tableName, new TableMetaData(tableName, columnMetaDataList));
        }

        return baseSchema;
    }

    private Map<String, TableMetaData> addForeignKeyRelationships(Set<Class<?>> entities, Map<String, TableMetaData> baseSchema) {
        Map<String, TableMetaData> completeSchema = new HashMap<>(baseSchema);

        for (Class<?> entityClass : entities) {
            Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
            String tableName = entityAnnotation.tableName().isEmpty() ?
                    entityClass.getSimpleName().toLowerCase() : entityAnnotation.tableName();

            List<ColumnMetaData> columnMetaDataList = new ArrayList<>(baseSchema.get(tableName).columns());

            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Relationship.class)) {
                    processRelationship(field, tableName, columnMetaDataList, completeSchema);
                }
            }

            completeSchema.put(tableName, new TableMetaData(tableName, columnMetaDataList));
        }

        return completeSchema;
    }


    private void loadCurrentSchema() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            if (tableName.equalsIgnoreCase("sys_config")) {
                continue;
            }

            List<ColumnMetaData> columnMetaDataList = new ArrayList<>();

            ResultSet columns = metaData.getColumns(null, null, tableName, "%");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");

                if (isSystemColumn(columnName)) {
                    columnMetaDataList.add(new ColumnMetaData(
                            columnName,
                            columnType,
                            "",
                            columnSize,
                            false,
                            false,
                            false,
                            null
                    ));
                }
            }

            ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName);
            while (primaryKeys.next()) {
                String pkColumnName = primaryKeys.getString("COLUMN_NAME");
                columnMetaDataList = columnMetaDataList.stream()
                        .map(col -> col.name().equals(pkColumnName) ?
                                new ColumnMetaData(col.name(), col.type(), col.defaultValue(), col.length(), true, col.unique(), col.fKey(), col.tableReference())
                                : col)
                        .collect(Collectors.toList());
            }


            ResultSet foreignKeys = metaData.getImportedKeys(null, null, tableName);
            while (foreignKeys.next()) {
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                String refTableName = foreignKeys.getString("PKTABLE_NAME");

                columnMetaDataList = columnMetaDataList.stream()
                        .map(col -> col.name().equals(fkColumnName) ?
                                new ColumnMetaData(col.name(), col.type(), col.defaultValue(), col.length(), col.pKey(), col.unique(), true, refTableName)
                                : col)
                        .collect(Collectors.toList());
            }

            currentSchema.put(tableName, new TableMetaData(tableName, columnMetaDataList));
        }

    }


    private ColumnMetaData getColumnMetaData(Field field) {
        Column column = field.getAnnotation(Column.class);
        String columnName = column.name().isEmpty() ? field.getName() : column.name();
        String columnType = column.type().isEmpty() ? getJdbcType(field.getType(), column) : column.type();

        return new ColumnMetaData(
                columnName,
                columnType,
                column.defaultValue(),
                column.length(),
                column.primaryKey(),
                column.unique(),
                false,
                null
        );
    }

    private void processRelationship(Field field, String tableName, List<ColumnMetaData> columnMetaDataList, Map<String, TableMetaData> schema) {
        Relationship relationship = field.getAnnotation(Relationship.class);
        String referencedTable = relationship.targetEntity().getSimpleName().toLowerCase();
        String foreignKeyType = "VARCHAR(36)";

        switch (relationship.type()) {
            case ONE_TO_ONE:
            case ONE_TO_MANY:
                String foreignKeyColumn = referencedTable + "_id";
                ColumnMetaData oneToManyForeignKey = new ColumnMetaData(
                        foreignKeyColumn,
                        foreignKeyType,
                        "",
                        36,
                        false,
                        false,
                        true,
                        tableName
                );
                columnMetaDataList.add(oneToManyForeignKey);
                break;
            case MANY_TO_ONE:
                String columnName = field.getName() + "_id";
                ColumnMetaData foreignKey = new ColumnMetaData(
                        columnName,
                        foreignKeyType,
                        "",
                        36,
                        false,
                        false,
                        true,
                        referencedTable
                );
                columnMetaDataList.add(foreignKey);
                break;

            case MANY_TO_MANY:
                String junctionTableName = tableName + "_" + referencedTable;
                List<ColumnMetaData> junctionColumns = getColumnMetaData(tableName, referencedTable);

                schema.put(junctionTableName, new TableMetaData(junctionTableName, junctionColumns));
                break;
        }
    }

    private static List<ColumnMetaData> getColumnMetaData(String tableName, String referencedTable) {
        List<ColumnMetaData> junctionColumns = new ArrayList<>();
        String foreignKeyType = "VARCHAR(36)";

        junctionColumns.add(new ColumnMetaData(
                tableName + "_id",
                foreignKeyType,
                "",
                36,
                false,
                false,
                true,
                tableName
        ));

        junctionColumns.add(new ColumnMetaData(
                referencedTable + "_id",
                foreignKeyType,
                "",
                36,
                false,
                false,
                true,
                referencedTable
        ));
        return junctionColumns;
    }

    private List<String> generateMigrations(Map<String, TableMetaData> currentSchema, Map<String, TableMetaData> newSchema) {
        List<String> migrations = new ArrayList<>();

        for (Map.Entry<String, TableMetaData> entry : newSchema.entrySet()) {
            String tableName = entry.getKey();
            TableMetaData newTable = entry.getValue();

            if (!currentSchema.containsKey(tableName)) {
                migrations.add(generateCreateTableSQL(tableName, newTable));
            } else {
                TableMetaData currentTable = currentSchema.get(tableName);
                migrations.addAll(generateColumnMigrations(tableName, currentTable, newTable));
            }
        }

        return migrations;
    }

    private List<String> generateForeignKeyMigrations(Map<String, TableMetaData> baseSchema, Map<String, TableMetaData> completeSchema) {
        List<String> migrations = new ArrayList<>();

        for (Map.Entry<String, TableMetaData> entry : completeSchema.entrySet()) {
            String tableName = entry.getKey();
            TableMetaData completeTable = entry.getValue();
            TableMetaData baseTable = baseSchema.get(tableName);
            TableMetaData currentTable = currentSchema.get(tableName);

            if (baseTable != null) {
                for (ColumnMetaData column : completeTable.columns()) {
                    if (column.fKey() && !currentTable.hasColumn(column.name())) {
                        migrations.add(String.format("ALTER TABLE %s ADD COLUMN %s %s;",
                                tableName, column.name(), column.type()));
                        migrations.add(String.format("ALTER TABLE %s ADD CONSTRAINT fk_%s_%s FOREIGN KEY (%s) REFERENCES %s(id);",
                                tableName, tableName, column.name(), column.name(), column.tableReference()));
                    }
                }
            }
        }

        return migrations;
    }



    private String generateCreateTableSQL(String tableName, TableMetaData table) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (");

        List<String> columnDefs = new ArrayList<>();
        List<String> constraints = new ArrayList<>();

        for (ColumnMetaData column : table.columns()) {
            columnDefs.add(generateColumnDefinition(column));

            if (column.pKey()) {
                constraints.add("PRIMARY KEY (" + column.name() + ")");
            }
            if (column.unique()) {
                constraints.add("UNIQUE (" + column.name() + ")");
            }
            if (column.fKey()) {
                constraints.add("FOREIGN KEY (" + column.name() + ") " +
                        "REFERENCES " + column.tableReference() + "(id)");
            }
        }

        sql.append(String.join(", ", columnDefs));
        if (!constraints.isEmpty()) {
            sql.append(", ").append(String.join(", ", constraints));
        }

        sql.append(");");
        return sql.toString();
    }

    private String generateColumnDefinition(ColumnMetaData column) {
        StringBuilder def = new StringBuilder(column.name() + " " + column.type());

        if (column.type().startsWith("VARCHAR") && !column.pKey()) {
            def.append("(").append(column.length()).append(")");
        }

        if (column.defaultValue() != null && !column.defaultValue().isEmpty()) {
            def.append(" DEFAULT ").append(column.defaultValue());
        }

        return def.toString();
    }


    private List<String> generateColumnMigrations(
            String tableName,
            TableMetaData currentTable,
            TableMetaData newTable
    ) {
        List<String> migrations = new ArrayList<>();
        Set<String> foreignKeyColumns = currentTable.columns().stream()
                .filter(ColumnMetaData::fKey)
                .map(ColumnMetaData::name)
                .collect(Collectors.toSet());


        for (ColumnMetaData currentColumn : currentTable.columns()) {
            if (isSystemColumn(currentColumn.name()) && !currentColumn.fKey() &&
                    !newTable.hasColumn(currentColumn.name())) {
                migrations.add(String.format("ALTER TABLE %s DROP COLUMN %s;",
                        tableName, currentColumn.name()));
            }
        }

        for (ColumnMetaData newColumn : newTable.columns()) {
            if(foreignKeyColumns.contains(newColumn.name())) continue;
            if (!currentTable.hasColumn(newColumn.name())) {
                migrations.add(String.format("ALTER TABLE %s ADD COLUMN %s %s%s;",
                        tableName,
                        newColumn.name(),
                        newColumn.type(),
                        newColumn.type().equals("VARCHAR") ? "(" + newColumn.length() + ")" : ""
                ));
            } else {
                ColumnMetaData currentColumn = currentTable.getColumn(newColumn.name());
                if (!columnsMatch(currentColumn, newColumn)) {
                    migrations.add(String.format("ALTER TABLE %s MODIFY COLUMN %s %s%s;",
                            tableName,
                            newColumn.name(),
                            newColumn.type(),
                            newColumn.type().equals("VARCHAR") ? "(" + newColumn.length() + ")" : ""
                    ));
                }
            }
        }

        return migrations;
    }

    private boolean isSystemColumn(String columnName) {
        Set<String> systemColumns = Set.of(
                "Host", "User", "plugin", "authentication_string", "password_expired",
                "password_last_changed", "password_lifetime", "account_locked", "Password_reuse_history",
                "Password_reuse_time", "Password_require_current", "User_attributes"

        );

        return !systemColumns.contains(columnName) && !columnName.endsWith("priv") && !columnName.startsWith("ssl") && !columnName.startsWith("x509") && !columnName.startsWith("max");
    }

    private boolean columnsMatch(ColumnMetaData current, ColumnMetaData expected) {
        return current.type().equalsIgnoreCase(expected.type()) &&
                (current.length() == expected.length() || !expected.type().equals("VARCHAR")) &&
                current.pKey() == expected.pKey() &&
                current.unique() == expected.unique();
    }

    private String getJdbcType(Class<?> type, Column column) {
        if(column.primaryKey()) return "VARCHAR(36) DEFAULT (UUID()) NOT NULL";
        if (type == String.class) return "VARCHAR";
        if (type == Integer.class || type == int.class) return "INTEGER";
        if (type == Long.class || type == long.class) return "BIGINT";
        if (type == Boolean.class || type == boolean.class) return "BOOLEAN";
        if (type == Double.class || type == double.class) return "DOUBLE";
        if (type == Float.class || type == float.class) return "FLOAT";
        if (type == Date.class) return "TIMESTAMP";
        return "VARCHAR";
    }
}
