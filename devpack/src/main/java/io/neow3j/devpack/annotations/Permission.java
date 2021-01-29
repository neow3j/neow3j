package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Permission.Permissions;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Repeatable(Permissions.class)
public @interface Permission {

    String contract();

    String[] methods() default "*";

    @Target(ElementType.TYPE)
    @interface Permissions {

        Permission[] value();

    }

}
