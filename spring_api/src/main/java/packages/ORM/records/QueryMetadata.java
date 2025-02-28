package packages.ORM.records;

import java.util.List;

public record QueryMetadata(String operation, List<String> properties) {}
