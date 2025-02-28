package packages.Json;

import packages.Json.Annotation.JsonField;
import packages.Json.Parser.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonMapper {
    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String jsonString, TypeReference<T> typeRef) {
        try {
            JsonParser parser = new JsonParser(jsonString);
            JsonValue jsonValue = parser.parse();

            Type type = typeRef.getType();
            if (jsonValue.isArray()) {
                Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                return (T) Utils.parseList(jsonValue.asArray(), (Class<?>) elementType);
            } else if (jsonValue.isObject()) {
                if(type instanceof Class<?> targetClass && targetClass.isRecord()){
                    return (T) Utils.parseRecord(jsonValue.asObject(), (Class<?>) type);
                }else if (type instanceof Class<?> ){
                    return (T) Utils.parseClass(jsonValue.asObject(), (Class<?>) type);
                }
            }
            throw new IllegalArgumentException("Invalid JSON structure");
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON: " + e.getMessage(), e);
        }
    }

    public static String toJson(Object object) {
        try {
            JsonObject jsonObject = new JsonObject();

            if (object.getClass().isRecord()) {
                Utils.makeJsonRecord(object, jsonObject);
            } else {
                Utils.makeJsonClass(object, jsonObject);
            }

            return jsonObject.toString();
        }catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field in " + object.getClass().getName(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid object structure: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }



}
