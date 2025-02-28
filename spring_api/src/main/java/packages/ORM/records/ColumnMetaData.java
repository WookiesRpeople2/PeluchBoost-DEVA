package packages.ORM.records;

import java.util.Objects;

public record ColumnMetaData(String name, String type, String defaultValue, int length, boolean pKey, boolean unique, boolean fKey, String tableReference) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColumnMetaData that)) return false;
        return length == that.length &&
                pKey == that.pKey &&
                unique == that.unique &&
                fKey == that.fKey &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(tableReference, that.tableReference  );
    }
}
