package io.neow3j.devpack.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface ManifestFeature {

    boolean hasStorage() default false;

    boolean payable() default false;

}
