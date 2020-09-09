package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used to mark a method to be replaced by an {@link io.neow3j.constants.OpCode#SYSCALL} to {@link
 * io.neow3j.constants.InteropServiceCode#SYSTEM_CONTRACT_CALL} with a contract script hash. The
 * annotated method can then be conveniently used in a smart contract to call another smart contract
 * with the defined script hash.
 * <p>
 * The method's body is ignored by the NeoVM compiler if it has this annotation.
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Appcall {

    /**
     * @return the script hash of the contract to call.
     */
    String scriptHash();

}
