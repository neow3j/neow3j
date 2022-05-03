package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used on smart contract methods to mark them as safe. This means that if another contract calls the annotated
 * method no event will be triggered. For methods not marked safe events are triggered on external calls.
 */
@Target(ElementType.METHOD)
public @interface Safe {

}