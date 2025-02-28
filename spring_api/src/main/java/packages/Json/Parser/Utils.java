package packages.Json.Parser;

import packages.Json.Annotation.JsonField;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Utils {

    public static void addValueToJsonObject(JsonObject jsonObject, String key, Object value) {
        switch (value) {
            case String s -> jsonObject.put(key, s);
            case Number number -> jsonObject.put(key, number);
            case Boolean b -> jsonObject.put(key, b);
            case null, default -> {
                assert value != null;
                jsonObject.put(key, value.toString());
            }
        }
    }

    public static Object convertValue(JsonValue jsonValue, Class<?> targetType) throws Exception {
        if (jsonValue == null || jsonValue.isNull()) return null;
        if (targetType == String.class && jsonValue.isString()) {
            return jsonValue.asString();
        }
        if ((targetType == int.class || targetType == Integer.class) && jsonValue.isNumber()) {
            return jsonValue.asNumber().intValue();
        }
        if ((targetType == long.class || targetType == Long.class) && jsonValue.isNumber()) {
            return jsonValue.asNumber().longValue();
        }
        if ((targetType == double.class || targetType == Double.class) && jsonValue.isNumber()) {
            return jsonValue.asNumber().doubleValue();
        }
        if ((targetType == boolean.class || targetType == Boolean.class) && jsonValue.isBoolean()) {
            return jsonValue.asBoolean();
        }

        throw new Exception("Unsupported type conversion: " + jsonValue.getClass() + " to " + targetType);
    }

    public static <T> List<T> parseList(List<JsonValue> jsonArray, Class<T> targetClass) {
        List<T> list = new ArrayList<>();
        for (JsonValue item : jsonArray) {
            if (item.isObject()) {
                if(targetClass.isRecord()){
                    list.add(parseRecord(item.asObject(), targetClass));
                }else{
                    list.add(parseClass(item.asObject(), targetClass));
                }
            } else {
                throw new IllegalArgumentException("Expected JSON object inside array, found: " + item);
            }
        }
        return list;
    }

    public static <T> T parseRecord(JsonObject jsonObject, Class<T> recordClass) {
            try {
                RecordComponent[] components = recordClass.getRecordComponents();
                Object[] constructorArgs = new Object[components.length];

                for (int i = 0; i < components.length; i++) {
                    RecordComponent component = components[i];
                    Pair<JsonField, String> pair = getRecordAnnotations(component);

                    if (pair.getFirst() != null) {
                        if (jsonObject.has(pair.getSecond())) {
                            JsonValue value = jsonObject.get(pair.getSecond());
                            constructorArgs[i] = convertValue(value, component.getType());
                        } else if (pair.getFirst().required()) {
                            throw new Exception("Required field " + pair.getSecond() + " not found in JSON");
                        }
                    } else {
                        String jsonKey = component.getName();
                        if (jsonObject.has(jsonKey)) {
                            JsonValue value = jsonObject.get(jsonKey);
                            constructorArgs[i] = convertValue(value, component.getType());
                        }
                    }
                }

                Constructor<T> constructor = recordClass.getDeclaredConstructor(
                        Arrays.stream(components)
                                .map(RecordComponent::getType)
                                .toArray(Class<?>[]::new)
                );

                return constructor.newInstance(constructorArgs);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to create an instance", e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid JSON structure: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing JSON", e);
            }
    }

    public static <T> T parseClass(JsonObject jsonObject, Class<T> targetClass) {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();

            for (Field field : targetClass.getDeclaredFields()) {
                Pair<JsonField, String> pair = getClassAnnotaions(field);
                if (pair.getFirst() != null) {
                    if (jsonObject.has(pair.getSecond())) {
                        JsonValue value = jsonObject.get(pair.getSecond());
                        field.set(instance, convertValue(value, field.getType()));
                    } else if (pair.getFirst().required()) {
                        throw new Exception("Required field " + pair.getSecond() + " not found in JSON");
                    }
                }
            }

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create an instance of " + targetClass.getName(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid JSON structure: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }

    public static void makeJsonRecord(Object object, JsonObject jsonObject) throws Exception {
        for (RecordComponent component : object.getClass().getRecordComponents()) {
            Pair<JsonField, String> pair = getRecordAnnotations(component);
            Method accessor = component.getAccessor();
            Object value = accessor.invoke(object);
            checkValueOnJsonMakeReq(value, pair.getFirst(), jsonObject, pair.getSecond());
        }
    }

    public static void makeJsonClass(Object object, JsonObject jsonObject) throws Exception {
        for (Field field : object.getClass().getDeclaredFields()) {
            Pair<JsonField, String> pair = getClassAnnotaions(field);
            if (pair.getFirst() != null) {
                Object value = field.get(object);
                checkValueOnJsonMakeReq(value, pair.getFirst(), jsonObject, pair.getSecond());

            }
        }
    }

    private static void checkValueOnJsonMakeReq(Object value, JsonField annotation, JsonObject jsonObject, String jsonKey) throws Exception {
        if (value != null) {
                Utils.addValueToJsonObject(jsonObject, jsonKey, value);
        } else {
            if (annotation.required()) {
                throw new Exception("Required field " + jsonKey + " is null");
            }
        }
    }

    private static Pair<JsonField, String> getRecordAnnotations(RecordComponent component){
        JsonField annotation = component.getAnnotation(JsonField.class);
        String jsonKey = annotation != null && !annotation.value().isEmpty() ?
                annotation.value() : component.getName();
        return new Pair<>(annotation, jsonKey);

    }

    private static Pair<JsonField, String> getClassAnnotaions(Field field){
        JsonField annotation = field.getAnnotation(JsonField.class);
        String jsonKey = null;
        if(annotation != null) {
            field.setAccessible(true);
            jsonKey = annotation.value().isEmpty() ? field.getName() : annotation.value();
        }
        return new Pair<>(annotation, jsonKey);
    }
}

