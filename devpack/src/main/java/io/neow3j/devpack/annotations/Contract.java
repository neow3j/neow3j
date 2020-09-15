package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used to mark a class as a smart contract on the Neo blockchain, which can then be used to make
 * calls to the contract's methods.
 * <p>
 * Examples of how to use this annotation can be found in {@link io.neow3j.devpack.neo.NEO},
 * {@link io.neow3j.devpack.neo.GAS}, {@link io.neow3j.devpack.neo.Policy}.
 */
@Target(ElementType.TYPE)
public @interface Contract {

    /**
     * @return the script hash of the contract to call.
     */
    String scriptHash();

}
