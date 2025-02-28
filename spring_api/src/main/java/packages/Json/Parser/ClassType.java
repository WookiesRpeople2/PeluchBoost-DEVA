package packages.Json.Parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ClassType {
    public static <T> TypeReference<T> typeReference(Class<?> rawClass, Class<?>... typeArgs) {
        Type type = createParameterizedType(rawClass, typeArgs);
        return new TypeReference<T>() {
            @Override
            public Type getType() {
                return type;
            }
        };
    }

    private static Type createParameterizedType(Class<?> rawClass, Class<?>... typeArgs){
        if (typeArgs.length == 0) {
            return rawClass;
        } else {
            return new ParameterizedType() {
                @Override
                public Type[] getActualTypeArguments() {
                    return typeArgs;
                }

                @Override
                public Type getRawType() {
                    return rawClass;
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }
            };
        }
    }
}
