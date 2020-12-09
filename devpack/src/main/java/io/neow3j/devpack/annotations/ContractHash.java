package io.neow3j.devpack.annotations;

import io.neow3j.devpack.GAS;
import io.neow3j.devpack.NEO;
import io.neow3j.devpack.Policy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used to mark a class as a smart contract on the Neo blockchain, which can then be used to make
 * calls to the contract's methods.
 * <p>
 * Examples of how to use this annotation can be found in {@link NEO}, {@link GAS}, {@link Policy}.
 */
@Target(ElementType.TYPE)
public @interface ContractHash {

    /**
     * @return the script hash of the contract to call.
     */
    String value();

}
