package io.neow3j.devpack.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Manifest {

    String name() default "default name";
    String description() default "default description";
    String author();
    
}
