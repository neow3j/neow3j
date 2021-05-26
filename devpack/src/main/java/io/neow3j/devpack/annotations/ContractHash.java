package io.neow3j.devpack.annotations;

import io.neow3j.devpack.contracts.GasToken;
import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.devpack.contracts.PolicyContract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used to set the contract hash on a {@link io.neow3j.devpack.contracts.ContractInterface},
 * which is then usable as a gateway to the actual contract on the blockchain.
 * <p>
 * The script hash has to be set in big-endian order.
 * <p>
 * Examples of how to use this annotation can be found in {@link NeoToken} and other native
 * contract interfaces.
 */
@Target(ElementType.TYPE)
public @interface ContractHash {

    /**
     * @return the script hash of the contract to call in big-endian order.
     */
    String value();

}
