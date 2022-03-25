package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation indicates that a class will be used as a NeoVM Struct.
 * <p>
 * Every Java class you intend to use in your smart contract class needs to be annotated with {code @Struct}
 * in order for the compilation to be successful. This is because instances of Java classes are simple structs on the
 * NeoVM. There is no notion of an object on NeoVM.
 */
@Target(ElementType.TYPE)
public @interface Struct {

}
