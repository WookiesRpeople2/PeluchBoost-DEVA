package packages.ORM.records;

import java.util.List;

public record TableMetaData(String tableName, List<ColumnMetaData> columns) {
    public boolean hasColumn(String name) {
        return columns.stream().anyMatch(c -> c.name().equals(name));
    }

    public ColumnMetaData getColumn(String name) {
        return columns.stream()
                .filter(c -> c.name().equals(name))
                .findFirst()
                .orElse(null);
    }
}
