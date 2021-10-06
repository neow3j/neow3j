package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Trust.Trusts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * This annotation is used on contract class level to define which contracts your contract trusts,
 * meaning for which contracts or groups of contracts will the user interface not give any
 * warnings if they call your contract.
 * <p>
 * The value can be a contract hash, a group's public key or a wildcard "*". The wildcard means
 * that any other contract is trusted.
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
