package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Trust.Trusts;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * This annotation is used on contract class level to represent contracts trusted to be called.
 * <p>
 * <ul>
 * <li>The value is a contract hash or group public key.</li>
 * <li>It can also be assigned with a wildcard "*". If it is a wildcard "*", then it means that it
 * trusts any contract.</li>
 * <li>If a contract is trusted, the user interface will not give any warnings when called by the
 * contract.</li>
 * </ul>
 * <p>
 * Usage of this annotation is not mandatory.
 */
@Target(ElementType.TYPE)
@Repeatable(Trusts.class)
public @interface Trust {

    String value();

    @Target(ElementType.TYPE)
    @interface Trusts {

        Trust[] value();

    }

}
