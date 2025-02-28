package packages.ORM.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name() default "";
    String type() default "";
    String defaultValue() default "";
    boolean primaryKey() default false;
    boolean unique() default false;
    int length() default 255;
}
