package io.neow3j.devpack.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used to define features of a smart contract. The attributes will be written to the
 * contract's manifest file.
 */
@Target(ElementType.TYPE)
public @interface Features {

    boolean hasStorage() default false;

    boolean payable() default false;

}
