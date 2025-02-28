package packages.Json.Parser;

import java.util.HashMap;
import java.util.Map;

public class JsonObject {
    private final Map<String, JsonValue> members = new HashMap<>();

    public void put(String key, JsonValue value) {
        members.put(key, value);
    }

    public void put(String key, String value) {
        members.put(key, new JsonValue(value));
    }

    public void put(String key, Number value) {
        members.put(key, new JsonValue(value));
    }

    public void put(String key, Boolean value) {
        members.put(key, new JsonValue(value));
    }

    public JsonValue get(String key) {
        return members.get(key);
    }

    public boolean has(String key) {
        return members.containsKey(key);
    }

    public int size() {
        return members.size();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        for (Map.Entry<String, JsonValue> entry : members.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(entry.getValue());
        }

        sb.append("}");
        return sb.toString();
    }
}
