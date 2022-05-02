package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Trust.Trusts;
import io.neow3j.devpack.constants.NativeContract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * This annotation is used on contract class level to define which contracts your contract trusts, meaning for which
 * contracts or groups of contracts will the user interface not give any warnings if they call your contract.
 */
@Target(ElementType.TYPE)
@Repeatable(Trusts.class)
public @interface Trust {

    /**
     * Indicates which contract or contract group is trusted. It can be a hash of a contract, a public key of a
     * contract group, or the wildcard "*", which includes all contracts.
     * <p>
     * If the field {@code nativeContract} is used, this field may not be used. Otherwise, it is mandatory.
     *
     * @return the contract hash or group key.
     */
    String contract() default "";

    /**
     * Indicates which native contract is trusted.
     * <p>
     * If the field {@code contract} is used, this field may not be used. Otherwise, it is mandatory.
     *
     * @return the native contract.
     */
    NativeContract nativeContract() default NativeContract.None;

    @Target(ElementType.TYPE)
    @interface Trusts {

        Trust[] value();

    }

}
