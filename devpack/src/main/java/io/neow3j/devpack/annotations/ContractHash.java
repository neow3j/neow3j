package io.neow3j.devpack.annotations;

import io.neow3j.devpack.contracts.GasToken;
import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.devpack.contracts.PolicyContract;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used to mark a class as a smart contract on the Neo blockchain, which can then be used to make
 * calls to the contract's methods.
 * <p>
 * Examples of how to use this annotation can be found in {@link NeoToken}, {@link GasToken}, {@link PolicyContract}.
 */
@Target(ElementType.TYPE)
public @interface ContractHash {

    /**
     * @return the script hash of the contract to call.
     */
    String value();

}
