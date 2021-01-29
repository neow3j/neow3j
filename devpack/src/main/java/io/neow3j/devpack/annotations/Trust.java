package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Trust.Trusts;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Repeatable(Trusts.class)
public @interface Trust {

    String value();

    @Target(ElementType.TYPE)
    @interface Trusts {

        Trust[] value();

    }

}
