package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Use this annotation to set the parameter names of an event. These will be used in the contract's ABI and will
 * increase developer experience for anyone interacting with the contract. If the annotation is not used, event
 * parameters will be named {@code arg1}, {@code arg2}, etc.
 */
@Target(ElementType.FIELD)
public @interface EventParameterNames {

    // No default value because it is mandatory to set it if used.
    String[] value();

}
