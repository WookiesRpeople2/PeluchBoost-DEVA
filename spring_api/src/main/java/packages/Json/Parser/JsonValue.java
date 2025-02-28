package packages.Json.Parser;


import java.util.List;

public class JsonValue {
    private final Object value;

    public JsonValue(Object value) {
        this.value = value;
    }

    public boolean isString() {
        return value instanceof String;
    }

    public boolean isNumber() {
        return value instanceof Number;
    }

    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isArray(){
        return value instanceof List<?>;
    }

    public boolean isObject(){
        return value instanceof JsonObject;
    }

    public String asString() {
        if (value == null) return null;
        return value.toString();
    }

    public Number asNumber() {
        switch (value) {
            case null -> {
                return null;
            }
            case Number number -> {
                return number;
            }
            case String ignored -> {
                try {
                    if (((String) value).contains(".")) {
                        return Double.parseDouble((String) value);
                    } else {
                        return Long.parseLong((String) value);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot convert " + value + " to a number");
                }
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to a number");
    }

    public Boolean asBoolean() {
        switch (value) {
            case null -> {
                return null;
            }
            case Boolean b -> {
                return b;
            }
            case String s -> {
                String str = s.toLowerCase();
                if (str.equals("true")) return true;
                if (str.equals("false")) return false;
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to a boolean");
    }

    @SuppressWarnings("unchecked")
    public List<JsonValue> asArray() {
        if (!isArray()) {
            throw new IllegalStateException("JsonValue is not a JSON Array");
        }
        return (List<JsonValue>) value;
    }

    public JsonObject asObject() {
        if (!isObject()) {
            throw new IllegalStateException("JsonValue is not a JsonObject");
        }
        return (JsonObject) value;
    }

    @Override
    public String toString() {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeString((String) value) + "\"";
        return value.toString();
    }

    public Object getValue() {
        return value;
    }

    private String escapeString(String str) {
        return str.replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
