package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Syscall;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CALL;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_GETCALLFLAGS;

/**
 * Represents a Neo smart contract and provides several contract-related methods for use in smart
 * contracts.
 */
public class Contract {

    /**
     * The contract's ID. Each contract is assigned an ID at deployment time and is fixed.
     */
    public final int id;

    /**
     * The number of times the contract has been updated.
     */
    public final int updateCounter;

    /**
     * The contract's hash.
     */
    public final Hash160 hash;

    /**
     * The contract's NEF.
     */
    public final byte[] nef;

    /**
     * The contract's manifest in JSON format.
     */
    public final String manifest;

    private Contract() {
        id = 0;
        updateCounter = 0;
        hash = new Hash160(new byte[0]);
        nef = new byte[0];
        manifest = null;
    }

    /**
     * Makes a call to the {@code method} of the contract with the {@code scriptHash} passing the
     * {@code arguments}.
     *
     * @param scriptHash The script hash of the contract to invoke.
     * @param method     The method to call.
     * @param callFlags  The {@link CallFlags} to use for the call.
     * @param arguments  The arguments to hand to the method.
     * @return the value returned by the contract method call.
     */
    @Syscall(SYSTEM_CONTRACT_CALL)
    public static native Object call(Hash160 scriptHash, String method, byte callFlags,
            Object[] arguments);

    /**
     * Gets the call flags with which the contract has been called. I.e., use this to know with
     * which call flags your contract is being called.
     *
     * @return the call flags encoded in one byte.
     */
    @Syscall(SYSTEM_CONTRACT_GETCALLFLAGS)
    public static native byte getCallFlags();

}
